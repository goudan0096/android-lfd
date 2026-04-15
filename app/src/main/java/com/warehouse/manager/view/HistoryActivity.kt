package com.warehouse.manager.view

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.warehouse.manager.databinding.ActivityHistoryBinding
import com.warehouse.manager.ui.adapter.StockRecordAdapter
import com.warehouse.manager.ui.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 历史记录查看页面
 * 支持按商品查询和按位置查询
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: StockRecordAdapter

    // 查询模式：true = 按商品查询，false = 按位置查询
    private var isQueryByProduct = true

    // 筛选条件
    private var filterProductKeyword: String? = null
    private var filterAction: String? = null
    private var filterLocation: String? = null
    private var filterStartTime: Long? = null
    private var filterEndTime: Long? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        setupToolbar()
        setupFilterViews()
        setupRecyclerView()
        loadLocations()
        loadData() // 首次加载全部数据
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupFilterViews() {
        // 操作类型下拉框（按商品查询）
        val actions = arrayOf("全部", "入库", "出库")
        val actionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, actions)
        binding.autoCompleteAction.setAdapter(actionAdapter)

        // 操作类型下拉框（按位置查询）
        val locationActionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, actions)
        binding.autoCompleteLocationAction.setAdapter(locationActionAdapter)

        // 查询模式切换
        binding.btnQueryByProduct.setOnClickListener {
            switchToProductMode()
        }

        binding.btnQueryByLocation.setOnClickListener {
            switchToLocationMode()
        }

        // 开始日期
        binding.btnStartTime.setOnClickListener {
            showDatePicker(true)
        }

        // 结束日期
        binding.btnEndTime.setOnClickListener {
            showDatePicker(false)
        }

        // 查询按钮
        binding.btnSearch.setOnClickListener {
            updateFilters()
            loadData()
        }
    }

    private fun switchToProductMode() {
        isQueryByProduct = true
        binding.productFilterContainer.visibility = View.VISIBLE
        binding.locationFilterContainer.visibility = View.GONE

        // 更新按钮样式
        binding.btnQueryByProduct.setBackgroundColor(getColor(com.warehouse.manager.R.color.primary))
        binding.btnQueryByProduct.setTextColor(getColor(com.warehouse.manager.R.color.white))
        binding.btnQueryByLocation.setBackgroundColor(getColor(android.R.color.transparent))
        binding.btnQueryByLocation.setTextColor(getColor(com.warehouse.manager.R.color.primary))

        // 清空位置筛选
        binding.autoCompleteLocation.setText("", false)
        filterLocation = null
    }

    private fun switchToLocationMode() {
        isQueryByProduct = false
        binding.productFilterContainer.visibility = View.GONE
        binding.locationFilterContainer.visibility = View.VISIBLE

        // 更新按钮样式
        binding.btnQueryByLocation.setBackgroundColor(getColor(com.warehouse.manager.R.color.primary))
        binding.btnQueryByLocation.setTextColor(getColor(com.warehouse.manager.R.color.white))
        binding.btnQueryByProduct.setBackgroundColor(getColor(android.R.color.transparent))
        binding.btnQueryByProduct.setTextColor(getColor(com.warehouse.manager.R.color.primary))

        // 清空商品关键词筛选
        binding.etProductKeyword.text?.clear()
        filterProductKeyword = null
    }

    private fun updateFilters() {
        if (isQueryByProduct) {
            filterProductKeyword = binding.etProductKeyword.text.toString().trim().ifEmpty { null }

            val actionText = binding.autoCompleteAction.text.toString()
            filterAction = when (actionText) {
                "入库" -> "IN"
                "出库" -> "OUT"
                else -> null
            }
        } else {
            val locationText = binding.autoCompleteLocation.text.toString()
            filterLocation = if (locationText.isEmpty()) null else locationText

            val actionText = binding.autoCompleteLocationAction.text.toString()
            filterAction = when (actionText) {
                "入库" -> "IN"
                "出库" -> "OUT"
                else -> null
            }
        }
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
            val distinctLocations = locations.distinct()

            val locationAdapter = ArrayAdapter(
                this@HistoryActivity,
                android.R.layout.simple_dropdown_item_1line,
                distinctLocations
            )
            binding.autoCompleteLocation.setAdapter(locationAdapter)
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            val records = if (isQueryByProduct) {
                // 按商品关键词查询
                withContext(Dispatchers.IO) {
                    viewModel.getStockRecordsByKeyword(
                        keyword = filterProductKeyword,
                        action = filterAction,
                        location = null,
                        startTime = filterStartTime,
                        endTime = filterEndTime,
                        page = 1,
                        pageSize = 1000
                    )
                }
            } else {
                // 按位置查询
                withContext(Dispatchers.IO) {
                    viewModel.getStockRecordsWithFilters(
                        productCode = null,
                        productName = null,
                        action = filterAction,
                        location = filterLocation,
                        startTime = filterStartTime,
                        endTime = filterEndTime,
                        page = 1,
                        pageSize = 1000
                    )
                }
            }

            val totalRecords = if (isQueryByProduct) {
                withContext(Dispatchers.IO) {
                    viewModel.getStockRecordsCountByKeyword(
                        keyword = filterProductKeyword,
                        action = filterAction,
                        location = null,
                        startTime = filterStartTime,
                        endTime = filterEndTime
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    viewModel.getStockRecordsCountWithFilters(
                        productCode = null,
                        productName = null,
                        action = filterAction,
                        location = filterLocation,
                        startTime = filterStartTime,
                        endTime = filterEndTime
                    )
                }
            }

            runOnUiThread {
                adapter.submitList(records)
                updateEmptyState(records.isEmpty())

                val queryType = if (isQueryByProduct) "商品" else "位置"
                binding.tvRecordCount.text = "$queryType 共 $totalRecords 条记录"
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = StockRecordAdapter(showProductInfo = true) { record ->
            // 点击跳转到商品详情
            AddEditProductActivity.start(this, record.productId)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        private const val EXTRA_LOCATION = "extra_location"

        fun startForProduct(context: Context, productId: Long) {
            val intent = Intent(context, HistoryActivity::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, productId)
            context.startActivity(intent)
        }

        fun startForLocation(context: Context, location: String) {
            val intent = Intent(context, HistoryActivity::class.java)
            intent.putExtra(EXTRA_LOCATION, location)
            context.startActivity(intent)
        }

        fun start(context: Context) {
            context.startActivity(Intent(context, HistoryActivity::class.java))
        }
    }
}
