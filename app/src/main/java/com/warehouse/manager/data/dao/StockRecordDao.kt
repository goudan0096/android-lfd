package com.warehouse.manager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.warehouse.manager.data.model.StockRecord

/**
 * 出入库记录数据访问对象
 */
@Dao
interface StockRecordDao {

    @Insert
    suspend fun insert(record: StockRecord): Long

    @Query("SELECT * FROM stock_records WHERE productId = :productId ORDER BY timestamp DESC")
    fun getRecordsByProduct(productId: Long): LiveData<List<StockRecord>>

    @Query("SELECT * FROM stock_records WHERE location = :location ORDER BY timestamp DESC")
    fun getRecordsByLocation(location: String): LiveData<List<StockRecord>>

    @Query("SELECT * FROM stock_records ORDER BY timestamp DESC")
    fun getAllRecords(): LiveData<List<StockRecord>>

    @Query("SELECT * FROM stock_records ORDER BY timestamp DESC")
    suspend fun getAllRecordsSync(): List<StockRecord>

    @Query("SELECT * FROM stock_records WHERE productId = :productId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsByProductPaged(productId: Long, limit: Int, offset: Int): List<StockRecord>

    @Query("SELECT * FROM stock_records WHERE location = :location ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsByLocationPaged(location: String, limit: Int, offset: Int): List<StockRecord>

    @Query("""
        SELECT * FROM stock_records
        WHERE (:productCode IS NULL OR productCode LIKE '%' || :productCode || '%')
        AND (:productName IS NULL OR productName LIKE '%' || :productName || '%')
        AND (:action IS NULL OR action = :action)
        AND (:location IS NULL OR location = :location)
        AND (:startTime IS NULL OR timestamp >= :startTime)
        AND (:endTime IS NULL OR timestamp <= :endTime)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecordsWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): List<StockRecord>

    /**
     * 按商品关键词查询（支持编码或名称模糊匹配）
     */
    @Query("""
        SELECT * FROM stock_records
        WHERE (:keyword IS NULL OR productCode LIKE '%' || :keyword || '%' OR productName LIKE '%' || :keyword || '%')
        AND (:action IS NULL OR action = :action)
        AND (:location IS NULL OR location = :location)
        AND (:startTime IS NULL OR timestamp >= :startTime)
        AND (:endTime IS NULL OR timestamp <= :endTime)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecordsByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): List<StockRecord>

    /**
     * 按商品关键词查询记录数量
     */
    @Query("""
        SELECT COUNT(*) FROM stock_records
        WHERE (:keyword IS NULL OR productCode LIKE '%' || :keyword || '%' OR productName LIKE '%' || :keyword || '%')
        AND (:action IS NULL OR action = :action)
        AND (:location IS NULL OR location = :location)
        AND (:startTime IS NULL OR timestamp >= :startTime)
        AND (:endTime IS NULL OR timestamp <= :endTime)
    """)
    suspend fun getCountByKeyword(
        keyword: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int

    @Query("""
        SELECT COUNT(*) FROM stock_records
        WHERE (:productCode IS NULL OR productCode LIKE '%' || :productCode || '%')
        AND (:productName IS NULL OR productName LIKE '%' || :productName || '%')
        AND (:action IS NULL OR action = :action)
        AND (:location IS NULL OR location = :location)
        AND (:startTime IS NULL OR timestamp >= :startTime)
        AND (:endTime IS NULL OR timestamp <= :endTime)
    """)
    suspend fun getRecordsCountWithFilters(
        productCode: String?,
        productName: String?,
        action: String?,
        location: String?,
        startTime: Long?,
        endTime: Long?
    ): Int

    @Query("SELECT COUNT(*) FROM stock_records WHERE productId = :productId")
    suspend fun getCountByProduct(productId: Long): Int

    @Query("SELECT COUNT(*) FROM stock_records WHERE location = :location")
    suspend fun getCountByLocation(location: String): Int

    @Query("SELECT DISTINCT location FROM stock_records ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<String>

    @Query("DELETE FROM stock_records WHERE productId = :productId")
    suspend fun deleteRecordsByProduct(productId: Long)

    @Query("SELECT COUNT(*) FROM stock_records WHERE productId = :productId AND action = 'IN'")
    suspend fun getInCountByProduct(productId: Long): Int

    @Query("SELECT COUNT(*) FROM stock_records WHERE productId = :productId AND action = 'OUT'")
    suspend fun getOutCountByProduct(productId: Long): Int
}
