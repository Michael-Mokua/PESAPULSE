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
            binding.tvDate.text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
            
            val isIncome = transaction.type in listOf("received", "deposit")
            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = "$prefix KES ${String.format("%.2f", transaction.amount)}"
            binding.tvAmount.setTextColor(
                if (isIncome) binding.root.context.getColor(android.R.color.holo_green_dark)
                else binding.root.context.getColor(android.R.color.holo_red_dark)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem == newItem
    }
}
