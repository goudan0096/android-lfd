package com.warehouse.manager.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * Android 原生定位辅助类
 * 使用系统 LocationManager，不依赖 Google Play Services
 * 支持 GPS 和网络定位，在国内可正常使用
 */
class LocationHelper(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var androidLocationListener: android.location.LocationListener? = null
    private var listener: LocationListener? = null
    private var hasResult = false

    interface LocationListener {
        fun onLocationResult(latitude: Double, longitude: Double)
        fun onLocationError(errorCode: Int, errorMessage: String)
    }

    /**
     * 设置定位结果监听器
     */
    fun setLocationListener(listener: LocationListener) {
        this.listener = listener
    }

    /**
     * 开始定位（单次）
     */
    fun startLocation() {
        // 如果已经返回过结果，不再处理
        if (hasResult) {
            return
        }

        if (locationManager == null) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        // 检查权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            listener?.onLocationError(-3, "缺少定位权限")
            return
        }

        // 检查定位服务是否开启
        val isGpsEnabled = try {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        } catch (e: Exception) {
            false
        }
        val isNetworkEnabled = try {
            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (e: Exception) {
            false
        }

        if (!isGpsEnabled && !isNetworkEnabled) {
            listener?.onLocationError(-1, "定位服务未开启，请在设置中开启定位服务")
            return
        }

        androidLocationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!hasResult) {
                    hasResult = true
                    try {
                        listener?.onLocationResult(location.latitude, location.longitude)
                    } catch (e: Exception) {
                        Log.e("LocationHelper", "Error in onLocationResult", e)
                    }
                    stopLocation()
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // 优先使用网络定位（室内、地下室也能定位）
            if (isNetworkEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    androidLocationListener!!,
                    Looper.getMainLooper()
                )
            }

            // 同时启用 GPS 定位（更精确）
            if (isGpsEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    androidLocationListener!!,
                    Looper.getMainLooper()
                )
            }

            // 尝试获取最后一次已知位置（快速返回）
            val lastKnownLocation = getLastKnownLocation()
            if (lastKnownLocation != null && !hasResult) {
                hasResult = true
                try {
                    listener?.onLocationResult(lastKnownLocation.latitude, lastKnownLocation.longitude)
                } catch (e: Exception) {
                    Log.e("LocationHelper", "Error in lastKnownLocation callback", e)
                }
                stopLocation()
            }
        } catch (e: SecurityException) {
            listener?.onLocationError(-4, "定位权限被拒绝")
        } catch (e: Exception) {
            listener?.onLocationError(-2, "定位失败: ${e.message}")
        }
    }

    /**
     * 获取最后已知位置
     */
    private fun getLastKnownLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        try {
            val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            // 返回最新的位置
            return when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        hasResult = true
        androidLocationListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * 销毁定位客户端
     */
    fun destroy() {
        stopLocation()
        androidLocationListener = null
        listener = null
        locationManager = null
    }
}