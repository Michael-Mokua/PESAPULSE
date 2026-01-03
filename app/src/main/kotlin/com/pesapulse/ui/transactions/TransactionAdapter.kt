package com.pesapulse.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pesapulse.data.model.TransactionEntity
import com.pesapulse.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter : ListAdapter<TransactionEntity, TransactionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: TransactionEntity) {
            binding.tvCounterparty.text = transaction.counterparty
            
            val df = java.text.DecimalFormat("#,##0.00")
            val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val isIncome = transaction.type in listOf("received", "deposit")
            val prefix = if (isIncome) "+" else "-"
            
            binding.tvAmount.text = "$prefix KES ${df.format(transaction.amount)}"
            binding.tvAmount.setTextColor(if (isIncome) 
                android.graphics.Color.parseColor("#00FF88") else android.graphics.Color.parseColor("#FF3D00"))
            
            binding.tvCategoryDate.text = "${transaction.category} • ${dateFormat.format(Date(transaction.timestamp))}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem == newItem
    }
}
