package com.warehouse.manager.data.repository

import androidx.lifecycle.LiveData
import com.warehouse.manager.data.dao.StockRecordDao
import com.warehouse.manager.data.model.StockRecord

/**
 * 出入库记录仓库
 */
class StockRecordRepository(private val stockRecordDao: StockRecordDao) {

    fun getAllRecords(): LiveData<List<StockRecord>> = stockRecordDao.getAllRecords()

    fun getRecordsByProduct(productId: Long): LiveData<List<StockRecord>> {
        return stockRecordDao.getRecordsByProduct(productId)
    }

    fun getRecordsByLocation(location: String): LiveData<List<StockRecord>> {
        return stockRecordDao.getRecordsByLocation(location)
    }

    suspend fun insert(record: StockRecord): Long {
        return stockRecordDao.insert(record)
    }

    suspend fun deleteRecordsByProduct(productId: Long) {
        stockRecordDao.deleteRecordsByProduct(productId)
    }

    suspend fun getRecordsByProductPaged(productId: Long, page: Int, pageSize: Int): List<StockRecord> {
        return stockRecordDao.getRecordsByProductPaged(productId, pageSize, (page - 1) * pageSize)
    }

    suspend fun getRecordsByLocationPaged(location: String, page: Int, pageSize: Int): List<StockRecord> {
        return stockRecordDao.getRecordsByLocationPaged(location, pageSize, (page - 1) * pageSize)
    }

    suspend fun getRecordsWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        page: Int,
        pageSize: Int
    ): List<StockRecord> {
        return stockRecordDao.getRecordsWithFilters(
            productCode, productName, action, location, startTime, endTime,
            pageSize, (page - 1) * pageSize
        )
    }

    suspend fun getRecordsByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        page: Int,
        pageSize: Int
    ): List<StockRecord> {
        return stockRecordDao.getRecordsByKeyword(
            keyword, action, location, startTime, endTime,
            pageSize, (page - 1) * pageSize
        )
    }

    suspend fun getCountByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int {
        return stockRecordDao.getCountByKeyword(keyword, action, location, startTime, endTime)
    }

    suspend fun getRecordsCountWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int {
        return stockRecordDao.getRecordsCountWithFilters(
            productCode, productName, action, location, startTime, endTime
        )
    }

    suspend fun getCountByProduct(productId: Long): Int {
        return stockRecordDao.getCountByProduct(productId)
    }

    suspend fun getCountByLocation(location: String): Int {
        return stockRecordDao.getCountByLocation(location)
    }

    suspend fun getAllLocations(): List<String> {
        return stockRecordDao.getAllLocations()
    }

    suspend fun getAllRecordsSync(): List<StockRecord> {
        return stockRecordDao.getAllRecordsSync()
    }
}
