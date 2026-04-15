package com.warehouse.manager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 操作类型：入库或出库
 */
enum class StockAction {
    IN,     // 入库
    OUT     // 出库
}

/**
 * 出入库记录实体
 * @property id 主键 ID
 * @property productId 关联商品 ID
 * @property productCode 商品编码（冗余，方便查询）
 * @property productName 商品名称（冗余）
 * @property location 操作时的位置
 * @property action 操作类型：IN=入库，OUT=出库
 * @property timestamp 操作时间戳
 * @property note 备注
 */
@Entity(
    tableName = "stock_records",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId"), Index("location"), Index("timestamp")]
)
data class StockRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val productId: Long,

    val productCode: String,

    val productName: String = "",

    val location: String,

    val action: StockAction,

    val timestamp: Long = System.currentTimeMillis(),

    val note: String = ""
)
