package com.warehouse.manager.view

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import com.warehouse.manager.R
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.databinding.ActivityNavigationBinding
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import java.util.*

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var viewModel: ProductViewModel
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListenerImpl? = null

    private var product: Product? = null
    private var productId: Long = 0

    private var blinkTimer: Timer? = null
    private var currentBlinkInterval: Long = 2000

    // 跟踪活动状态，防止在销毁后更新UI
    private var isActivityActive = false
    // 标记是否已获取过位置，避免重复Toast
    private var hasLocated = false
    // 防止重复注册定位监听
    private var isRequestingLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isActivityActive = true

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
        if (!isActivityActive || isRequestingLocation) {
            return
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (!hasLocated) {
            Toast.makeText(this, "正在获取位置...", Toast.LENGTH_SHORT).show()
        }

        stopLocationUpdates()
        isRequestingLocation = true

        locationListener = LocationListenerImpl { location ->
            if (!isActivityActive) return@LocationListenerImpl
            updateNavigation(location.latitude, location.longitude)
        }

        try {
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

            if (!isGpsEnabled && !isNetworkEnabled) {
                isRequestingLocation = false
                Toast.makeText(this, "定位服务未开启，请在设置中开启定位服务", Toast.LENGTH_LONG).show()
                return
            }

            val provider = when {
                isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
                isGpsEnabled -> LocationManager.GPS_PROVIDER
                else -> null
            }

            provider?.let {
                locationManager?.requestLocationUpdates(
                    it,
                    3000L,
                    3f,
                    locationListener!!,
                    Looper.getMainLooper()
                )
            }

            getLastKnownLocation()?.let { location ->
                updateNavigation(location.latitude, location.longitude)
            }

        } catch (e: Exception) {
            isRequestingLocation = false
            e.printStackTrace()
            Toast.makeText(this, "定位服务启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLastKnownLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        return try {
            val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun stopLocationUpdates() {
        isRequestingLocation = false
        locationListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (e: Exception) {
                // 忽略可能发生的异常
            }
        }
        locationListener = null
    }

    private fun updateNavigation(currentLat: Double, currentLon: Double) {
        if (!isActivityActive) return
        val product = this.product ?: return

        if (!hasLocated) {
            hasLocated = true
            isRequestingLocation = false
        }

        val distance = viewModel.calculateDistance(currentLat, currentLon, product.latitude, product.longitude)

        runOnUiThread {
            if (!isActivityActive) return@runOnUiThread
            binding.tvCurrentLocation.text = "纬度：%.5f, 经度：%.5f".format(currentLat, currentLon)
            binding.tvDistance.text = "%.1f ${getString(R.string.meters)}".format(distance)
            binding.tvSignalStrength.text = viewModel.getSignalStrength(distance)

            updateSignalBars(distance)

            val newInterval = viewModel.getBlinkInterval(distance)
            if (newInterval != currentBlinkInterval) {
                currentBlinkInterval = newInterval
                startBlinking(newInterval)
            }
        }
    }

    private fun updateSignalBars(distance: Float) {
        if (!isActivityActive) return
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
            view.animate().cancel()
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
                    if (!isActivityActive) {
                        this.cancel()
                        return
                    }
                    runOnUiThread {
                        if (isActivityActive) {
                            pulseProductDot()
                            pulseCircles()
                        }
                    }
                }
            }, 0, intervalMs)
        }
    }

    private fun pulseProductDot() {
        if (!isActivityActive) return
        val alphaAnim = AlphaAnimation(1.0f, 0.2f).apply {
            duration = currentBlinkInterval / 2
            repeatCount = 1
            repeatMode = android.view.animation.Animation.REVERSE
        }
        binding.viewProductDot.startAnimation(alphaAnim)
    }

    private fun pulseCircles() {
        if (!isActivityActive) return
        val circles = listOf(binding.viewCircle1, binding.viewCircle2, binding.viewCircle3)
        circles.forEachIndexed { index, circle ->
            circle.animate().cancel()
            val delay = index * 50L
            circle.postDelayed({
                if (!isActivityActive) return@postDelayed
                val animatorSet = android.animation.AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(circle, View.SCALE_X, 1.0f, 1.2f, 1.0f),
                        ObjectAnimator.ofFloat(circle, View.SCALE_Y, 1.0f, 1.2f, 1.0f),
                        ObjectAnimator.ofFloat(circle, View.ALPHA, 0.1f + index * 0.1f, 0.5f, 0.1f + index * 0.1f)
                    )
                    duration = currentBlinkInterval
                }
                animatorSet.start()
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
        isActivityActive = false
        stopLocationUpdates()
        blinkTimer?.cancel()
        blinkTimer = null
    }

    override fun onResume() {
        super.onResume()
        isActivityActive = true
        isRequestingLocation = false
        if (product != null) {
            checkLocationPermissionAndStart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityActive = false
        stopLocationUpdates()
        blinkTimer?.cancel()
        blinkTimer = null
    }

    private class LocationListenerImpl(
        private val onLocationChangedCallback: (Location) -> Unit
    ) : LocationListener {

        override fun onLocationChanged(location: Location) {
            onLocationChangedCallback(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
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
