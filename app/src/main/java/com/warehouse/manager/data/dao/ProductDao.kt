package com.warehouse.manager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.warehouse.manager.data.model.Product

/**
 * 商品数据访问对象
 */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdLiveData(id: Long): LiveData<Product?>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    suspend fun getAllProductsSync(): List<Product>

    @Query("SELECT * FROM products WHERE code LIKE :query OR name LIKE :query OR location LIKE :query")
    fun searchProducts(query: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE code = :code")
    suspend fun getProductByCode(code: String): Product?

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): LiveData<Int>

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT * FROM products
        WHERE (:productCode IS NULL OR code LIKE '%' || :productCode || '%')
        AND (:productName IS NULL OR name LIKE '%' || :productName || '%')
        AND (:location IS NULL OR location = :location)
        ORDER BY updatedAt DESC
    """)
    suspend fun getProductsWithFilters(
        productCode: String?,
        productName: String?,
        location: String?
    ): List<Product>
}
