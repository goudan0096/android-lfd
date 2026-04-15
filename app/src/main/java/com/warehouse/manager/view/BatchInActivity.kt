package com.warehouse.manager.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.warehouse.manager.R
import com.warehouse.manager.data.model.BatchProductEntry
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.databinding.ActivityBatchInBinding
import com.warehouse.manager.ui.adapter.BatchProductAdapter
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import com.warehouse.manager.util.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 批量入库页面
 */
class BatchInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatchInBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: BatchProductAdapter
    private var locationHelper: LocationHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatchInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        // 默认添加一个空白条目
        adapter.addItem()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = BatchProductAdapter(
            onRemoveClick = { position ->
                adapter.removeItem(position)
            },
            onDataChanged = {
                updateProductCount()
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun updateProductCount() {
        binding.tvProductCount.text = "共 ${adapter.itemCount} 个"
    }

    private fun setupClickListeners() {
        binding.btnAddProduct.setOnClickListener {
            adapter.addItem()
        }

        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnSubmit.setOnClickListener {
            submitBatchIn()
        }
    }

    private fun getCurrentLocation() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                requestLocationUpdates()
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

        locationHelper = LocationHelper(this)
        locationHelper?.setLocationListener(object : LocationHelper.LocationListener {
            override fun onLocationResult(latitude: Double, longitude: Double) {
                runOnUiThread {
                    binding.etLatitude.setText(String.format("%.6f", latitude))
                    binding.etLongitude.setText(String.format("%.6f", longitude))
                    Toast.makeText(this@BatchInActivity, "位置已获取", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onLocationError(errorCode: Int, errorMessage: String) {
                runOnUiThread {
                    Toast.makeText(this@BatchInActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
        locationHelper?.startLocation()
    }

    private fun submitBatchIn() {
        val warehouseLocation = binding.etWarehouseLocation.text.toString().trim()
        if (warehouseLocation.isEmpty()) {
            Toast.makeText(this, "请输入仓储位置", Toast.LENGTH_SHORT).show()
            return
        }

        val latitude = binding.etLatitude.text.toString().toDoubleOrNull() ?: 0.0
        val longitude = binding.etLongitude.text.toString().toDoubleOrNull() ?: 0.0

        val entries = adapter.getItems().filter { it.code.isNotBlank() }
        if (entries.isEmpty()) {
            Toast.makeText(this, "请至少添加一个商品", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示确认对话框
        val productList = entries.joinToString("\n") { "- ${it.code}" }
        MaterialAlertDialogBuilder(this)
            .setTitle("确认入库")
            .setMessage("确定要入库 ${entries.size} 个商品吗？\n\n仓储位置：$warehouseLocation\n\n商品列表：\n$productList")
            .setPositiveButton("确定") { _, _ ->
                performBatchIn(entries, warehouseLocation, latitude, longitude)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performBatchIn(
        entries: List<BatchProductEntry>,
        warehouseLocation: String,
        latitude: Double,
        longitude: Double
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            var successCount = 0

            withContext(Dispatchers.IO) {
                for (entry in entries) {
                    // 检查商品是否已存在
                    val existingProduct = viewModel.getProductByCodeSync(entry.code)

                    if (existingProduct != null) {
                        // 商品已存在，更新状态为入库
                        val updatedProduct = existingProduct.copy(
                            status = ProductStatus.IN_WAREHOUSE,
                            location = warehouseLocation,
                            latitude = latitude,
                            longitude = longitude,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateSync(updatedProduct, StockAction.IN)
                        successCount++
                    } else {
                        // 新商品，创建并入库
                        val newProduct = Product(
                            code = entry.code,
                            name = entry.name,
                            location = warehouseLocation,
                            latitude = latitude,
                            longitude = longitude,
                            status = ProductStatus.IN_WAREHOUSE
                        )
                        viewModel.insertSync(newProduct, StockAction.IN)
                        successCount++
                    }
                }
            }

            Toast.makeText(this@BatchInActivity, "成功入库 $successCount 个商品", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper?.destroy()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        fun start(context: Context) {
            val intent = Intent(context, BatchInActivity::class.java)
            context.startActivity(intent)
        }
    }
}
