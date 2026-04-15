package com.warehouse.manager.data.database

import androidx.room.TypeConverter
import com.warehouse.manager.data.model.ProductStatus
import com.warehouse.manager.data.model.StockAction

/**
 * Room 类型转换器
 */
class Converters {

    @TypeConverter
    fun fromProductStatus(status: ProductStatus): String {
        return status.name
    }

    @TypeConverter
    fun toProductStatus(value: String): ProductStatus {
        return ProductStatus.valueOf(value)
    }

    @TypeConverter
    fun fromStockAction(action: StockAction): String {
        return action.name
    }

    @TypeConverter
    fun toStockAction(value: String): StockAction {
        return StockAction.valueOf(value)
    }
}
