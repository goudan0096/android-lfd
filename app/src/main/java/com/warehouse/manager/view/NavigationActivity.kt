package com.warehouse.manager.view

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.warehouse.manager.R
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.databinding.ActivityNavigationBinding
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import java.util.*

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var product: Product? = null
    private var productId: Long = 0

    private var blinkTimer: Timer? = null
    private var currentBlinkInterval: Long = 2000
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        productId = intent.getLongExtra(EXTRA_PRODUCT_ID, 0)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnUpdateLocation.setOnClickListener {
            startLocationUpdates()
        }

        loadProduct()
    }

    private fun loadProduct() {
        viewModel.getProductById(productId) { p ->
            p?.let {
                product = it
                runOnUiThread {
                    displayProductInfo(it)
                    checkLocationPermissionAndStart()
                }
            }
        }
    }

    private fun displayProductInfo(product: Product) {
        binding.tvProductCode.text = "商品编码：${product.code}"
        binding.tvProductName.text = product.name.takeIf { it.isNotEmpty() } ?: "暂无商品名称"
        binding.tvProductLocation.text = "位置：${product.location}"
        binding.tvCurrentLocation.text = "等待获取位置..."
    }

    private fun checkLocationPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
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
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        Toast.makeText(this, "正在获取位置...", Toast.LENGTH_SHORT).show()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).apply {
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(1000)
        }.build()

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    updateNavigation(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun updateNavigation(currentLat: Double, currentLon: Double) {
        val product = this.product ?: return

        binding.tvCurrentLocation.text = "纬度：%.5f, 经度：%.5f".format(currentLat, currentLon)

        val distance = viewModel.calculateDistance(currentLat, currentLon, product.latitude, product.longitude)
        binding.tvDistance.text = "%.1f ${getString(R.string.meters)}".format(distance)
        binding.tvSignalStrength.text = viewModel.getSignalStrength(distance)

        updateSignalBars(distance)

        val newInterval = viewModel.getBlinkInterval(distance)
        if (newInterval != currentBlinkInterval) {
            currentBlinkInterval = newInterval
            startBlinking(newInterval)
        }
    }

    private fun updateSignalBars(distance: Float) {
        val signals = listOf(binding.viewSignal1, binding.viewSignal2, binding.viewSignal3, binding.viewSignal4)
        val activeCount = when {
            distance <= 5 -> 4
            distance <= 10 -> 3
            distance <= 20 -> 2
            distance <= 50 -> 1
            else -> 0
        }

        signals.forEachIndexed { index, view ->
            view.alpha = if (index < activeCount) 1.0f else 0.2f
            val scaleAnim = ObjectAnimator.ofFloat(view, "scaleY", if (index < activeCount) 1.1f else 1.0f)
            scaleAnim.duration = 200
            scaleAnim.start()
        }
    }

    private fun startBlinking(intervalMs: Long) {
        blinkTimer?.cancel()
        blinkTimer = null

        if (intervalMs <= 0) return

        blinkTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        pulseProductDot()
                        pulseCircles()
                    }
                }
            }, 0, intervalMs)
        }
    }

    private fun pulseProductDot() {
        val alphaAnim = AlphaAnimation(1.0f, 0.2f).apply {
            duration = currentBlinkInterval / 2
            repeatCount = 1
            repeatMode = android.view.animation.Animation.REVERSE
        }
        binding.viewProductDot.startAnimation(alphaAnim)
    }

    private fun pulseCircles() {
        val circles = listOf(binding.viewCircle1, binding.viewCircle2, binding.viewCircle3)
        circles.forEachIndexed { index, circle ->
            val delay = index * 100L
            circle.postDelayed({
                val scaleX = ObjectAnimator.ofFloat(circle, View.SCALE_X, 1.0f, 1.2f, 1.0f)
                val scaleY = ObjectAnimator.ofFloat(circle, View.SCALE_Y, 1.0f, 1.2f, 1.0f)
                val alpha = ValueAnimator.ofFloat(0.1f + index * 0.1f, 0.5f, 0.1f + index * 0.1f)

                scaleX.duration = currentBlinkInterval
                scaleY.duration = currentBlinkInterval
                alpha.duration = currentBlinkInterval

                alpha.addUpdateListener {
                    circle.alpha = it.animatedValue as Float
                }

                scaleX.start()
                scaleY.start()
                alpha.start()
            }, delay)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                product?.let {
                    HistoryActivity.startForProduct(this, it.id)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        blinkTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (product != null) {
            checkLocationPermissionAndStart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blinkTimer?.cancel()
        blinkTimer = null
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001

        fun start(context: Context, product: Product) {
            val intent = Intent(context, NavigationActivity::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, product.id)
            context.startActivity(intent)
        }
    }
}
