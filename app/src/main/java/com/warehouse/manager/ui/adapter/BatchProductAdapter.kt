package com.warehouse.manager.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.warehouse.manager.data.model.BatchProductEntry
import com.warehouse.manager.databinding.ItemBatchProductBinding

/**
 * 批量入库商品适配器
 */
class BatchProductAdapter(
    private val onRemoveClick: (Int) -> Unit,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<BatchProductAdapter.ViewHolder>() {

    private val items = mutableListOf<BatchProductEntry>()

    fun addItem() {
        items.add(BatchProductEntry())
        notifyItemInserted(items.size - 1)
        onDataChanged()
    }

    fun removeItem(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyDataSetChanged()
            onDataChanged()
        }
    }

    fun getItems(): List<BatchProductEntry> = items.toList()

    fun isEmpty(): Boolean = items.isEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBatchProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemBatchProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var codeWatcher: TextWatcher? = null
        private var nameWatcher: TextWatcher? = null

        fun bind(item: BatchProductEntry, position: Int) {
            binding.tvIndex.text = (position + 1).toString()

            // 移除旧的 watcher
            codeWatcher?.let { binding.etCode.removeTextChangedListener(it) }
            nameWatcher?.let { binding.etName.removeTextChangedListener(it) }

            binding.etCode.setText(item.code)
            binding.etName.setText(item.name)

            // 添加新的 watcher
            codeWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < items.size) {
                        items[pos] = items[pos].copy(code = s?.toString() ?: "")
                        onDataChanged()
                    }
                }
            }

            nameWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < items.size) {
                        items[pos] = items[pos].copy(name = s?.toString() ?: "")
                        onDataChanged()
                    }
                }
            }

            binding.etCode.addTextChangedListener(codeWatcher)
            binding.etName.addTextChangedListener(nameWatcher)

            binding.btnRemove.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemoveClick(pos)
                }
            }
        }
    }
}
