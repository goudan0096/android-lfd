package com.warehouse.manager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.warehouse.manager.data.database.AppDatabase
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.data.model.StockRecord
import com.warehouse.manager.data.repository.ProductRepository
import com.warehouse.manager.data.repository.StockRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 商品 ViewModel
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository
    private val stockRecordRepository: StockRecordRepository
    val allProducts: LiveData<List<Product>>
    val productCount: LiveData<Int>
    val allStockRecords: LiveData<List<StockRecord>>

    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _eventNavigateToProduct = MutableLiveData<Event<Long>>()
    val eventNavigateToProduct: LiveData<Event<Long>> = _eventNavigateToProduct

    init {
        val database = AppDatabase.getDatabase(application)
        val productDao = database.productDao()
        val stockRecordDao = database.stockRecordDao()
        productRepository = ProductRepository(productDao)
        stockRecordRepository = StockRecordRepository(stockRecordDao)
        allProducts = productRepository.allProducts
        productCount = productRepository.productCount
        allStockRecords = stockRecordRepository.getAllRecords()

        // 监听搜索查询变化
        viewModelScope.launch {
            searchQuery.collect { query ->
                if (query.isEmpty()) {
                    // 空搜索时显示所有商品
                    productRepository.allProducts.observeForever { all ->
                        _searchResults.value = all
                    }
                } else {
                    productRepository.searchProducts(query).observeForever { results ->
                        _searchResults.value = results
                    }
                }
            }
        }
    }

    fun insert(product: Product, recordAction: StockAction? = null, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = productRepository.insert(product)
            // 如果是新商品且指定了操作类型，添加入库/出库记录
            if (recordAction != null) {
                val record = StockRecord(
                    productId = id,
                    productCode = product.code,
                    productName = product.name,
                    location = product.location,
                    action = recordAction
                )
                stockRecordRepository.insert(record)
            }
            onSuccess(id)
        }
    }

    fun update(product: Product, recordAction: StockAction? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val oldProduct = productRepository.getProductById(product.id)
            productRepository.update(product)
            // 如果状态变化或指定了操作类型，添加记录
            if (recordAction != null || (oldProduct?.status != product.status)) {
                val action = recordAction ?: if (product.status == ProductStatus.IN_WAREHOUSE)
                    StockAction.IN else StockAction.OUT
                val record = StockRecord(
                    productId = product.id,
                    productCode = product.code,
                    productName = product.name.ifEmpty { product.location },
                    location = product.location,
                    action = action
                )
                stockRecordRepository.insert(record)
            }
            onSuccess()
        }
    }

    fun delete(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            productRepository.delete(product)
            stockRecordRepository.deleteRecordsByProduct(product.id)
            onSuccess()
        }
    }

    fun deleteById(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            productRepository.deleteById(id)
            stockRecordRepository.deleteRecordsByProduct(id)
            onSuccess()
        }
    }

    fun getProductById(id: Long, callback: (Product?) -> Unit) {
        viewModelScope.launch {
            val product = productRepository.getProductById(id)
            callback(product)
        }
    }

    fun getProductByCode(code: String, callback: (Product?) -> Unit) {
        viewModelScope.launch {
            val product = productRepository.getProductByCode(code)
            callback(product)
        }
    }

    /**
     * 同步获取商品（用于协程中调用）
     */
    suspend fun getProductByCodeSync(code: String): Product? {
        return productRepository.getProductByCode(code)
    }

    /**
     * 同步插入商品
     */
    suspend fun insertSync(product: Product, recordAction: StockAction? = null): Long {
        val id = productRepository.insert(product)
        if (recordAction != null) {
            val record = StockRecord(
                productId = id,
                productCode = product.code,
                productName = product.name,
                location = product.location,
                action = recordAction
            )
            stockRecordRepository.insert(record)
        }
        return id
    }

    /**
     * 同步更新商品
     */
    suspend fun updateSync(product: Product, recordAction: StockAction? = null) {
        productRepository.update(product)
        if (recordAction != null) {
            val record = StockRecord(
                productId = product.id,
                productCode = product.code,
                productName = product.name.ifEmpty { product.location },
                location = product.location,
                action = recordAction
            )
            stockRecordRepository.insert(record)
        }
    }

    /**
     * 根据筛选条件获取商品列表
     */
    suspend fun getProductsWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): List<Product> {
        // 由于商品表没有 action 和时间字段，需要从 stock_records 获取关联
        // 这里简化处理，只根据商品属性筛选
        return productRepository.getProductsWithFilters(
            productCode = productCode,
            productName = productName,
            location = location
        )
    }

    fun getStockRecordsByProduct(productId: Long): LiveData<List<StockRecord>> {
        return stockRecordRepository.getRecordsByProduct(productId)
    }

    fun getStockRecordsByLocation(location: String): LiveData<List<StockRecord>> {
        return stockRecordRepository.getRecordsByLocation(location)
    }

    /**
     * 分页查询历史记录（带筛选条件）
     */
    suspend fun getStockRecordsWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        page: Int,
        pageSize: Int
    ): List<StockRecord> {
        return stockRecordRepository.getRecordsWithFilters(
            productCode, productName, action, location, startTime, endTime, page, pageSize
        )
    }

    /**
     * 按商品关键词查询记录
     */
    suspend fun getStockRecordsByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        page: Int,
        pageSize: Int
    ): List<StockRecord> {
        return stockRecordRepository.getRecordsByKeyword(
            keyword, action, location, startTime, endTime, page, pageSize
        )
    }

    /**
     * 按商品关键词查询记录数量
     */
    suspend fun getStockRecordsCountByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int {
        return stockRecordRepository.getCountByKeyword(keyword, action, location, startTime, endTime)
    }

    /**
     * 获取筛选后的记录总数
     */
    suspend fun getStockRecordsCountWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int {
        return stockRecordRepository.getRecordsCountWithFilters(
            productCode, productName, action, location, startTime, endTime
        )
    }

    /**
     * 获取所有位置列表
     */
    suspend fun getAllLocations(): List<String> {
        return stockRecordRepository.getAllLocations()
    }

    /**
     * 同步获取所有商品
     */
    suspend fun getAllProductsSync(): List<Product> {
        return productRepository.getAllProductsSync()
    }

    /**
     * 同步获取所有出入库记录
     */
    suspend fun getAllStockRecordsSync(): List<StockRecord> {
        return stockRecordRepository.getAllRecordsSync()
    }

    /**
     * 同步插入出入库记录
     */
    suspend fun insertStockRecordSync(record: StockRecord): Long {
        return stockRecordRepository.insert(record)
    }

    /**
     * 批量更新商品状态
     */
    fun batchUpdateStatus(products: Set<Product>, action: StockAction, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            for (product in products) {
                val newStatus = if (action == StockAction.IN) ProductStatus.IN_WAREHOUSE else ProductStatus.OUT_WAREHOUSE
                val updatedProduct = product.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                productRepository.update(updatedProduct)

                // 添加出入库记录
                val record = StockRecord(
                    productId = product.id,
                    productCode = product.code,
                    productName = product.name.ifEmpty { product.location },
                    location = product.location,
                    action = action
                )
                stockRecordRepository.insert(record)
            }
            onSuccess()
        }
    }

    fun setSelectedProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchProductByCode(code: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByCode(code)
            product?.let {
                _selectedProduct.value = it
                _eventNavigateToProduct.value = Event(it.id)
            }
        }
    }

    fun navigateToProduct(productId: Long) {
        _eventNavigateToProduct.value = Event(productId)
    }

    /**
     * 计算两个经纬度坐标之间的距离（单位：米）
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * 根据距离获取闪烁频率（距离越近，频率越高）
     * @return 闪烁间隔时间（毫秒），值越小表示闪烁越快
     */
    fun getBlinkInterval(distance: Float): Long {
        return when {
            distance <= 5 -> 100      // 5 米以内，100ms 闪烁一次（最快）
            distance <= 10 -> 200     // 10 米以内，200ms
            distance <= 20 -> 350     // 20 米以内，350ms
            distance <= 50 -> 500     // 50 米以内，500ms
            distance <= 100 -> 800    // 100 米以内，800ms
            distance <= 200 -> 1200   // 200 米以内，1200ms
            else -> 2000              // 200 米以上，2000ms（最慢）
        }
    }

    /**
     * 根据距离获取信号强度描述
     */
    fun getSignalStrength(distance: Float): String {
        return when {
            distance <= 5 -> "非常近"
            distance <= 10 -> "很近"
            distance <= 20 -> "较近"
            distance <= 50 -> "中等"
            distance <= 100 -> "较远"
            distance <= 200 -> "远"
            else -> "很远"
        }
    }
}

/**
 * 用于单次事件包装的类
 */
class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
