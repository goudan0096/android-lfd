package com.warehouse.manager.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.warehouse.manager.data.model.ExportData
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.data.model.StockRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * 数据导出导入管理器
 */
class DataManager(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * 导出所有数据到JSON格式
     */
    suspend fun exportData(
        products: List<Product>,
        stockRecords: List<StockRecord>
    ): String {
        return withContext(Dispatchers.Default) {
            val exportData = ExportData(
                version = 1,
                exportTime = System.currentTimeMillis(),
                products = products,
                stockRecords = stockRecords
            )
            gson.toJson(exportData)
        }
    }

    /**
     * 将JSON数据写入到URI（用于保存到文件）
     */
    suspend fun writeExportToUri(uri: Uri, jsonData: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(jsonData)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * 从URI读取JSON数据
     */
    suspend fun readImportFromUri(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 解析JSON数据为ExportData
     */
    suspend fun parseImportData(jsonString: String): ExportData? {
        return withContext(Dispatchers.Default) {
            try {
                gson.fromJson(jsonString, ExportData::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 获取导入数据的预览信息
     */
    fun getImportPreview(exportData: ExportData): ImportPreview {
        return ImportPreview(
            productCount = exportData.products.size,
            recordCount = exportData.stockRecords.size,
            exportTime = exportData.exportTime,
            version = exportData.version
        )
    }

    /**
     * 验证导入数据
     */
    fun validateImportData(exportData: ExportData): ValidationResult {
        val errors = mutableListOf<String>()

        // 验证商品数据
        val productCodes = mutableSetOf<String>()
        exportData.products.forEachIndexed { index, product ->
            if (product.code.isBlank()) {
                errors.add("商品 #${index + 1}: 编码不能为空")
            }
            if (productCodes.contains(product.code)) {
                errors.add("商品 #${index + 1}: 编码重复 (${product.code})")
            }
            productCodes.add(product.code)
        }

        // 验证出入库记录
        exportData.stockRecords.forEachIndexed { index, record ->
            if (record.productCode.isBlank()) {
                errors.add("记录 #${index + 1}: 商品编码不能为空")
            }
            if (record.location.isBlank()) {
                errors.add("记录 #${index + 1}: 位置不能为空")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

/**
 * 导入预览信息
 */
data class ImportPreview(
    val productCount: Int,
    val recordCount: Int,
    val exportTime: Long,
    val version: Int
)

/**
 * 数据验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
