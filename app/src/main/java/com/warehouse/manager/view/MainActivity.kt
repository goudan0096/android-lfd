package com.warehouse.manager.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.warehouse.manager.R
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.databinding.ActivityMainBinding
import com.warehouse.manager.ui.adapter.ProductAdapter
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter

    // 多选模式
    private var isMultiSelectMode = false

    // 筛选条件
    private var filterProductCode: String? = null
    private var filterProductName: String? = null
    private var filterAction: String? = null
    private var filterLocation: String? = null
    private var filterStartTime: Long? = null
    private var filterEndTime: Long? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        setupFilterViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        loadLocations()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_batch_in -> {
                BatchInActivity.start(this)
                true
            }
            R.id.action_history -> {
                HistoryActivity.start(this)
                true
            }
            R.id.action_settings -> {
                SettingsActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupFilterViews() {
        // 操作类型下拉框
        val actions = arrayOf("全部", "入库", "出库")
        val actionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, actions)
        binding.autoCompleteAction.setAdapter(actionAdapter)

        // 位置下拉框
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf("全部"))
        binding.autoCompleteLocation.setAdapter(locationAdapter)

        // 开始日期
        binding.btnStartTime.setOnClickListener {
            showDatePicker(true)
        }

        // 结束日期
        binding.btnEndTime.setOnClickListener {
            showDatePicker(false)
        }

        // 筛选按钮
        binding.btnFilter.setOnClickListener {
            updateFilters()
            loadData()
        }

        // 重置按钮
        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        // 批量出库按钮
        binding.btnMultiSelect.setOnClickListener {
            enterMultiSelectMode()
        }

        // 取消选择按钮
        binding.btnCancelSelect.setOnClickListener {
            exitMultiSelectMode()
        }

        // 批量出库提交按钮
        binding.btnBatchOut.setOnClickListener {
            submitBatchOut()
        }
    }

    private fun enterMultiSelectMode() {
        isMultiSelectMode = true
        adapter.toggleMultiSelectMode(true)

        // 隐藏筛选区域，显示底部操作栏
        binding.filterContainer.visibility = View.GONE
        binding.bottomActionBar.visibility = View.VISIBLE
        binding.bottomInfoBar.visibility = View.GONE
        binding.fabAdd.visibility = View.GONE

        // 更新按钮文字
        binding.btnMultiSelect.text = "取消批量出库"
        updateSelectedCount()
    }

    private fun exitMultiSelectMode() {
        isMultiSelectMode = false
        adapter.toggleMultiSelectMode(false)
        adapter.clearSelection()

        // 显示筛选区域，隐藏底部操作栏
        binding.filterContainer.visibility = View.VISIBLE
        binding.bottomActionBar.visibility = View.GONE
        binding.bottomInfoBar.visibility = View.VISIBLE
        binding.fabAdd.visibility = View.VISIBLE

        // 恢复按钮文字
        binding.btnMultiSelect.text = "批量出库"
    }

    private fun updateSelectedCount() {
        val count = adapter.getSelectedItems().size
        binding.tvSelectedCount.text = "已选择 $count 个商品"
        binding.btnBatchOut.isEnabled = count > 0
    }

    private fun submitBatchOut() {
        val products = adapter.getSelectedItems()
        if (products.isEmpty()) {
            Toast.makeText(this, "请先选择商品", Toast.LENGTH_SHORT).show()
            return
        }

        // 构建商品列表文本
        val productList = products.joinToString("\n") { "- ${it.code}" }

        MaterialAlertDialogBuilder(this)
            .setTitle("批量出库确认")
            .setMessage("确定要对以下 ${products.size} 个商品执行出库操作吗？\n\n$productList")
            .setPositiveButton("确认出库") { _, _ ->
                performBatchOut(products)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performBatchOut(products: Set<Product>) {
        viewModel.batchUpdateStatus(products, StockAction.OUT) {
            Toast.makeText(this, "已出库 ${products.size} 个商品", Toast.LENGTH_SHORT).show()
            exitMultiSelectMode()
            loadData()
        }
    }

    private fun updateFilters() {
        filterProductCode = binding.etProductCode.text.toString().trim().ifEmpty { null }
        filterProductName = binding.etProductName.text.toString().trim().ifEmpty { null }

        val actionText = binding.autoCompleteAction.text.toString()
        filterAction = when (actionText) {
            "入库" -> "IN"
            "出库" -> "OUT"
            else -> null
        }

        val locationText = binding.autoCompleteLocation.text.toString()
        filterLocation = if (locationText == "全部" || locationText.isEmpty()) null else locationText
    }

    private fun resetFilters() {
        binding.etProductCode.text?.clear()
        binding.etProductName.text?.clear()
        binding.autoCompleteAction.setText("全部", false)
        binding.autoCompleteLocation.setText("全部", false)
        binding.btnStartTime.text = "开始日期"
        binding.btnEndTime.text = "结束日期"

        filterProductCode = null
        filterProductName = null
        filterAction = null
        filterLocation = null
        filterStartTime = null
        filterEndTime = null

        loadData()
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                if (isStart) {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    selectedCalendar.set(Calendar.MINUTE, 0)
                    selectedCalendar.set(Calendar.SECOND, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)
                    filterStartTime = selectedCalendar.timeInMillis
                } else {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    selectedCalendar.set(Calendar.MINUTE, 59)
                    selectedCalendar.set(Calendar.SECOND, 59)
                    selectedCalendar.set(Calendar.MILLISECOND, 999)
                    filterEndTime = selectedCalendar.timeInMillis
                }

                val dateStr = dateFormat.format(selectedCalendar.time)
                if (isStart) {
                    binding.btnStartTime.text = dateStr
                } else {
                    binding.btnEndTime.text = dateStr
                }
            },
            year,
            month,
            day
        ).show()
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.Main).launch {
            val locations = withContext(Dispatchers.IO) {
                viewModel.getAllLocations()
            }
            val allLocations = mutableListOf("全部")
            allLocations.addAll(locations.distinct())

            val locationAdapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_dropdown_item_1line,
                allLocations
            )
            binding.autoCompleteLocation.setAdapter(locationAdapter)
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            val products = withContext(Dispatchers.IO) {
                viewModel.getProductsWithFilters(
                    productCode = filterProductCode,
                    productName = filterProductName,
                    action = filterAction,
                    location = filterLocation,
                    startTime = filterStartTime,
                    endTime = filterEndTime
                )
            }
            adapter.submitList(products)
            updateEmptyState(products.isEmpty())
            binding.tvProductCount.text = "共 ${products.size} 个商品"
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product ->
                if (!isMultiSelectMode) {
                    showProductOptions(product)
                }
            },
            onEditClick = { product ->
                AddEditProductActivity.start(this, product)
            },
            onNavigateClick = { product ->
                NavigationActivity.start(this, product)
            },
            onItemSelected = { _, _ ->
                // 更新选中数量
                updateSelectedCount()
            }
        )

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allProducts.observe(this) { products ->
            if (!isMultiSelectMode && !hasActiveFilters()) {
                adapter.submitList(products)
                updateEmptyState(products.isEmpty())
                binding.tvProductCount.text = "共 ${products.size} 个商品"
            }
        }

        viewModel.productCount.observe(this) { count ->
            if (!isMultiSelectMode && !hasActiveFilters()) {
                binding.tvProductCount.text = "共 $count 个商品"
            }
        }
    }

    private fun hasActiveFilters(): Boolean {
        return filterProductCode != null || filterProductName != null ||
               filterAction != null || filterLocation != null ||
               filterStartTime != null || filterEndTime != null
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            AddEditProductActivity.start(this)
        }
    }

    private fun showProductOptions(product: Product) {
        val options = arrayOf("查看/导航", "编辑", "出入库记录")
        MaterialAlertDialogBuilder(this)
            .setTitle(product.code)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> NavigationActivity.start(this, product)
                    1 -> AddEditProductActivity.start(this, product)
                    2 -> HistoryActivity.startForProduct(this, product.id)
                }
            }
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (!isMultiSelectMode) {
            loadData()
        }
    }

    override fun onBackPressed() {
        if (isMultiSelectMode) {
            exitMultiSelectMode()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
