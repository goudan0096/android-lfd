package com.warehouse.manager.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.warehouse.manager.R
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.databinding.ActivityAddEditProductBinding
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import com.warehouse.manager.util.LocationHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var viewModel: ProductViewModel
    private var locationHelper: LocationHelper? = null
    private var productId: Long = 0
    private var isEditMode = false
    private var selectedStatus: ProductStatus = ProductStatus.IN_WAREHOUSE
    private var cameraExecutor: ExecutorService? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var cameraProvider: ProcessCameraProvider? = null
    @kotlin.jvm.Volatile
    private var hasHandledScanResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        productId = intent.getLongExtra(EXTRA_PRODUCT_ID, 0)
        isEditMode = productId > 0

        binding.toolbar.title = if (isEditMode) getString(R.string.edit_product) else getString(R.string.add_product)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupStatusDropdown()

        if (isEditMode) {
            loadProduct()
        } else {
            // 新增模式：自动获取当前位置填充位置坐标
            getCurrentLocation()
        }

        setupClickListeners()
        setupBarcodeScanner()
    }

    private fun setupStatusDropdown() {
        val statuses = arrayOf(
            getString(R.string.status_in_warehouse),
            getString(R.string.status_out_warehouse)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        binding.autoCompleteStatus.setAdapter(adapter)
        binding.autoCompleteStatus.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = if (position == 0) ProductStatus.IN_WAREHOUSE else ProductStatus.OUT_WAREHOUSE
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        binding.btnGetCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnScanCode.setOnClickListener {
            checkCameraPermissionAndScan()
        }

        binding.btnCloseScan.setOnClickListener {
            stopCamera()
        }
    }

    private fun setupBarcodeScanner() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_ITF
            )
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                showScanView()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "需要相机权限才能使用扫码功能", Toast.LENGTH_LONG).show()
                requestCameraPermission()
            }
            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun showScanView() {
        if (cameraExecutor == null || cameraExecutor?.isShutdown == true) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        hasHandledScanResult = false
        binding.scanContainer.visibility = android.view.View.VISIBLE
        binding.previewView.visibility = android.view.View.VISIBLE
        binding.btnCloseScan.visibility = android.view.View.VISIBLE
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                        processBarcode(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processBarcode(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || hasHandledScanResult) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner?.process(image)
            ?.addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue } ?: return@addOnSuccessListener
                if (hasHandledScanResult) return@addOnSuccessListener
                hasHandledScanResult = true
                runOnUiThread {
                    binding.etCode.setText(rawValue)
                    Toast.makeText(this, "扫描成功：$rawValue", Toast.LENGTH_SHORT).show()
                    stopCamera()
                }
            }
            ?.addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        binding.previewView.visibility = android.view.View.GONE
        binding.scanContainer.visibility = android.view.View.GONE
        binding.btnCloseScan.visibility = android.view.View.GONE
        if (cameraExecutor?.isShutdown == false) {
            cameraExecutor?.shutdown()
        }
        cameraExecutor = null
    }

    private fun loadProduct() {
        viewModel.getProductById(productId) { product ->
            runOnUiThread {
                product?.let {
                    binding.etCode.setText(it.code)
                    binding.etName.setText(it.name)
                    binding.etLocation.setText(it.location)
                    binding.etLatitude.setText(it.latitude.toString())
                    binding.etLongitude.setText(it.longitude.toString())
                    selectedStatus = it.status
                    binding.autoCompleteStatus.setText(
                        if (it.status == ProductStatus.IN_WAREHOUSE)
                            getString(R.string.status_in_warehouse)
                        else
                            getString(R.string.status_out_warehouse),
                        false
                    )
                }
            }
        }
    }

    private fun saveProduct() {
        val code = binding.etCode.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val latitude = binding.etLatitude.text.toString().toDoubleOrNull() ?: 0.0
        val longitude = binding.etLongitude.text.toString().toDoubleOrNull() ?: 0.0

        if (code.isEmpty()) {
            binding.etCode.error = getString(R.string.please_enter_code)
            return
        }

        // 加载原商品数据以保留创建时间等字段
        viewModel.getProductById(productId) { oldProduct ->
            val product = Product(
                id = productId,
                code = code,
                name = name,
                location = location,
                latitude = latitude,
                longitude = longitude,
                status = selectedStatus,
                createdAt = oldProduct?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            runOnUiThread {
                if (isEditMode) {
                    val action = if (oldProduct?.status != selectedStatus) {
                        if (selectedStatus == ProductStatus.IN_WAREHOUSE) StockAction.IN else StockAction.OUT
                    } else null
                    viewModel.update(product, action) {
                        runOnUiThread {
                            Toast.makeText(this@AddEditProductActivity, R.string.product_saved, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    viewModel.insert(product, StockAction.IN) { _ ->
                        runOnUiThread {
                            Toast.makeText(this@AddEditProductActivity, R.string.product_saved, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentLocation() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                requestLocationUpdates()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show()
                requestLocationPermission()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        Toast.makeText(this, "正在获取位置...", Toast.LENGTH_SHORT).show()

        // 复用已有的 LocationHelper
        if (locationHelper == null) {
            locationHelper = LocationHelper(this)
        }

        locationHelper?.setLocationListener(object : LocationHelper.LocationListener {
            override fun onLocationResult(latitude: Double, longitude: Double) {
                runOnUiThread {
                    binding.etLatitude.setText(String.format("%.6f", latitude))
                    binding.etLongitude.setText(String.format("%.6f", longitude))
                    Toast.makeText(this@AddEditProductActivity, "位置已获取", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onLocationError(errorCode: Int, errorMessage: String) {
                runOnUiThread {
                    Toast.makeText(this@AddEditProductActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
        locationHelper?.startLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showScanView()
                } else {
                    Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        barcodeScanner?.close()
        locationHelper?.destroy()
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002

        fun start(context: Context) {
            val intent = Intent(context, AddEditProductActivity::class.java)
            context.startActivity(intent)
        }

        fun start(context: Context, product: Product) {
            val intent = Intent(context, AddEditProductActivity::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, product.id)
            context.startActivity(intent)
        }

        fun start(context: Context, productId: Long) {
            val intent = Intent(context, AddEditProductActivity::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, productId)
            context.startActivity(intent)
        }
    }
}
