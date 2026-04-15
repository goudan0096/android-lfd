package com.warehouse.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 商品状态枚举
 */
enum class ProductStatus {
    IN_WAREHOUSE,   // 入库
    OUT_WAREHOUSE   // 出库
}

/**
 * 商品实体类
 * @property id 主键 ID
 * @property code 商品编码
 * @property name 商品名称
 * @property location 仓储位置描述
 * @property latitude 纬度
 * @property longitude 经度
 * @property status 商品状态（入库/出库）
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val code: String,

    val name: String = "",

    val location: String,

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val status: ProductStatus = ProductStatus.IN_WAREHOUSE,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
