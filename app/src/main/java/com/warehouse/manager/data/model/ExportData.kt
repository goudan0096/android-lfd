package com.warehouse.manager.data.model

import com.google.gson.annotations.SerializedName

/**
 * 导出数据结构
 */
data class ExportData(
    @SerializedName("version")
    val version: Int = 1,

    @SerializedName("exportTime")
    val exportTime: Long = System.currentTimeMillis(),

    @SerializedName("products")
    val products: List<Product>,

    @SerializedName("stockRecords")
    val stockRecords: List<StockRecord>
)
