//Package: adapters
//file name: TransactionAdapter.kt

package com.example.budgettracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.R
import com.example.budgettracker.databinding.ItemTransactionBinding
import com.example.budgettracker.models.Transaction
import com.example.budgettracker.models.TransactionType
import com.example.budgettracker.utils.DateUtils

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                tvTitle.text = transaction.title
                tvCategory.text = transaction.category
                tvDate.text = DateUtils.formatDate(transaction.date)

                // Set amount color based on type
                val color = if (transaction.type == TransactionType.INCOME) {
                    ContextCompat.getColor(root.context, R.color.green)
                } else {
                    ContextCompat.getColor(root.context, R.color.red)
                }
                tvAmount.setTextColor(color)

                // Add prefix for income/expense
                if (transaction.type == TransactionType.INCOME) {
                    tvAmount.text = "+ ${DateUtils.formatCurrency(transaction.amount)}"
                } else {
                    tvAmount.text = "- ${DateUtils.formatCurrency(transaction.amount)}"
                }

                root.setOnClickListener {
                    onItemClick(transaction)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(transaction)
                }
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}