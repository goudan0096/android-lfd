package com.warehouse.manager.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warehouse.manager.R
import com.warehouse.manager.data.model.StockAction
import com.warehouse.manager.data.model.StockRecord
import java.text.SimpleDateFormat
import java.util.*

/**
 * 出入库记录适配器
 */
class StockRecordAdapter(
    private val showProductInfo: Boolean = true,
    private val onItemClick: ((StockRecord) -> Unit)? = null
) : ListAdapter<StockRecord, StockRecordAdapter.RecordViewHolder>(RecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAction: TextView = itemView.findViewById(R.id.tvAction)
        private val tvProductCode: TextView = itemView.findViewById(R.id.tvProductCode)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val viewActionIndicator: View = itemView.findViewById(R.id.viewActionIndicator)

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(record: StockRecord) {
            val isIn = record.action == StockAction.IN
            val actionText = if (isIn) "入库" else "出库"
            val colorRes = if (isIn) R.color.success else R.color.error

            tvAction.text = actionText
            tvAction.setTextColor(itemView.context.getColor(colorRes))
            viewActionIndicator.setBackgroundColor(itemView.context.getColor(colorRes))

            tvProductCode.text = if (record.productName.isNotEmpty())
                "${record.productCode} - ${record.productName}"
            else
                record.productCode
            tvProductCode.visibility = if (showProductInfo) View.VISIBLE else View.GONE

            tvLocation.text = record.location
            tvTime.text = dateFormat.format(Date(record.timestamp))

            // 点击事件
            itemView.setOnClickListener {
                onItemClick?.invoke(record)
            }
        }
    }

    class RecordDiffCallback : DiffUtil.ItemCallback<StockRecord>() {
        override fun areItemsTheSame(oldItem: StockRecord, newItem: StockRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StockRecord, newItem: StockRecord): Boolean {
            return oldItem == newItem
        }
    }
}
