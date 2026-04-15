package com.warehouse.manager.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.warehouse.manager.R
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.ProductStatus

/**
 * 商品列表适配器
 */
class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onNavigateClick: (Product) -> Unit,
    private val onLongClick: (Product) -> Unit = {},
    private var isMultiSelectMode: Boolean = false,
    private val onItemSelected: (Product, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private val selectedItems = mutableSetOf<Product>()

    fun toggleMultiSelectMode(enable: Boolean) {
        isMultiSelectMode = enable
        if (!enable) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Set<Product> {
        return selectedItems.toSet()
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCode: TextView = itemView.findViewById(R.id.tvCode)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnNavigate: ImageButton = itemView.findViewById(R.id.btnNavigate)
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)

        fun bind(product: Product) {
            tvCode.text = product.code
            tvName.text = product.name.ifEmpty { product.location }
            tvLocation.text = product.location

            // 设置状态
            chipStatus.text = when (product.status) {
                ProductStatus.IN_WAREHOUSE -> "入库"
                ProductStatus.OUT_WAREHOUSE -> "出库"
            }
            chipStatus.chipBackgroundColor = itemView.context.getColorStateList(
                if (product.status == ProductStatus.IN_WAREHOUSE) R.color.success else R.color.error
            )

            // 多选模式
            if (isMultiSelectMode) {
                cbSelect.visibility = View.VISIBLE
                cbSelect.isChecked = selectedItems.contains(product)
                itemView.setOnClickListener {
                    if (selectedItems.contains(product)) {
                        selectedItems.remove(product)
                        onItemSelected(product, false)
                    } else {
                        selectedItems.add(product)
                        onItemSelected(product, true)
                    }
                    notifyItemChanged(layoutPosition)
                }
                cbSelect.setOnClickListener {
                    if (selectedItems.contains(product)) {
                        selectedItems.remove(product)
                        onItemSelected(product, false)
                    } else {
                        selectedItems.add(product)
                        onItemSelected(product, true)
                    }
                }
                btnEdit.visibility = View.GONE
                btnNavigate.visibility = View.GONE
            } else {
                cbSelect.visibility = View.GONE
                btnEdit.visibility = View.VISIBLE
                btnNavigate.visibility = View.VISIBLE
                itemView.setOnClickListener { onItemClick(product) }
                btnEdit.setOnClickListener { onEditClick(product) }
                btnNavigate.setOnClickListener { onNavigateClick(product) }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
