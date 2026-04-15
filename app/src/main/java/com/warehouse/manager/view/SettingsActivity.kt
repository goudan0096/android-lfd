package com.warehouse.manager.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.warehouse.manager.R
import com.warehouse.manager.data.DataManager
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.data.model.StockRecord
import com.warehouse.manager.databinding.ActivitySettingsBinding
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据管理页面
 * 支持数据导出和导入
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var dataManager: DataManager

    private var products: List<Product> = emptyList()
    private var stockRecords: List<StockRecord> = emptyList()

    // 文件选择器启动器
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportToUri(it) }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        dataManager = DataManager(this)

        setupToolbar()
        setupClickListeners()
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnExport.setOnClickListener {
            startExport()
        }

        binding.btnImport.setOnClickListener {
            startImport()
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 加载商品数据
            products = withContext(Dispatchers.IO) {
                viewModel.getAllProductsSync()
            }

            // 加载出入库记录
            stockRecords = withContext(Dispatchers.IO) {
                viewModel.getAllStockRecordsSync()
            }

            // 更新统计显示
            binding.tvProductCount.text = products.size.toString()
            binding.tvRecordCount.text = stockRecords.size.toString()
        }
    }

    private fun startExport() {
        if (products.isEmpty() && stockRecords.isEmpty()) {
            Toast.makeText(this, "暂无数据可导出", Toast.LENGTH_SHORT).show()
            return
        }

        // 生成默认文件名
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "warehouse_backup_${dateFormat.format(Date())}.json"

        // 启动文件保存对话框
        createDocumentLauncher.launch(fileName)
    }

    private fun exportToUri(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 显示进度提示
                Toast.makeText(this@SettingsActivity, "正在导出数据...", Toast.LENGTH_SHORT).show()

                // 生成JSON数据
                val jsonData = withContext(Dispatchers.Default) {
                    dataManager.exportData(products, stockRecords)
                }

                // 写入文件
                val success = withContext(Dispatchers.IO) {
                    dataManager.writeExportToUri(uri, jsonData)
                }

                if (success) {
                    Toast.makeText(this@SettingsActivity, "数据导出成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "数据导出失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SettingsActivity, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startImport() {
        // 启动文件选择器
        openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
    }

    private fun importFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 读取文件内容
                val jsonString = withContext(Dispatchers.IO) {
                    dataManager.readImportFromUri(uri)
                }

                if (jsonString.isNullOrEmpty()) {
                    Toast.makeText(this@SettingsActivity, "文件读取失败", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 解析JSON数据
                val exportData = withContext(Dispatchers.Default) {
                    dataManager.parseImportData(jsonString)
                }

                if (exportData == null) {
                    Toast.makeText(this@SettingsActivity, "JSON解析失败，请检查文件格式", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 验证数据
                val validation = dataManager.validateImportData(exportData)
                if (!validation.isValid) {
                    val errorMsg = validation.errors.take(5).joinToString("\n")
                    Toast.makeText(this@SettingsActivity, "数据验证失败:\n$errorMsg", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 显示预览和确认对话框
                showImportPreview(exportData)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SettingsActivity, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImportPreview(exportData: com.warehouse.manager.data.model.ExportData) {
        val preview = dataManager.getImportPreview(exportData)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val exportDate = dateFormat.format(Date(preview.exportTime))

        val message = """
            版本: ${preview.version}
            导出时间: $exportDate

            商品数量: ${preview.productCount} 个
            出入库记录: ${preview.recordCount} 条

            是否确认导入？
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("导入预览")
            .setMessage(message)
            .setPositiveButton("确认导入") { _, _ ->
                performImport(exportData)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performImport(exportData: com.warehouse.manager.data.model.ExportData) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@SettingsActivity, "正在导入数据...", Toast.LENGTH_SHORT).show()

                var productImported = 0
                var recordImported = 0

                withContext(Dispatchers.IO) {
                    // 导入商品
                    for (product in exportData.products) {
                        // 检查是否已存在（根据编码判断）
                        val existing = viewModel.getProductByCodeSync(product.code)
                        if (existing == null) {
                            viewModel.insertSync(product, null)
                            productImported++
                        }
                    }

                    // 导入出入库记录
                    for (record in exportData.stockRecords) {
                        viewModel.insertStockRecordSync(record)
                        recordImported++
                    }
                }

                // 重新加载数据
                loadData()

                Toast.makeText(
                    this@SettingsActivity,
                    "导入完成！\n新增商品: $productImported\n新增记录: $recordImported",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SettingsActivity, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
