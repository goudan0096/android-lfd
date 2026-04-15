package com.warehouse.manager.data.repository

import androidx.lifecycle.LiveData
import com.warehouse.manager.data.dao.ProductDao
import com.warehouse.manager.data.model.Product

/**
 * 商品仓库类，负责数据操作
 */
class ProductRepository(private val productDao: ProductDao) {

    val allProducts: LiveData<List<Product>> = productDao.getAllProducts()
    val productCount: LiveData<Int> = productDao.getProductCount()

    suspend fun insert(product: Product): Long {
        return productDao.insert(product)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteById(id: Long) {
        productDao.deleteById(id)
    }

    suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)
    }

    suspend fun getProductByCode(code: String): Product? {
        return productDao.getProductByCode(code)
    }

    fun searchProducts(query: String): LiveData<List<Product>> {
        val searchQuery = "%${query}%"
        return productDao.searchProducts(searchQuery)
    }

    suspend fun getAllProductsSync(): List<Product> {
        return productDao.getAllProductsSync()
    }

    suspend fun getProductsWithFilters(
        productCode: String?,
        productName: String?,
        location: String?
    ): List<Product> {
        return productDao.getProductsWithFilters(productCode, productName, location)
    }
}
