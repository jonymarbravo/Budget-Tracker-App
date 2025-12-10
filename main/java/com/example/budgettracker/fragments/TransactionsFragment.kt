//Package: fragments
//TransactionsFragment.kt

package com.example.budgettracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.activities.AddTransactionActivity
import com.example.budgettracker.adapters.TransactionAdapter
import com.example.budgettracker.databinding.FragmentTransactionsBinding
import com.example.budgettracker.viewmodels.TransactionViewModel

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                val intent = Intent(requireContext(), AddTransactionActivity::class.java)
                intent.putExtra("transaction_id", transaction.id)
                startActivity(intent)
            },
            onDeleteClick = { transaction ->
                viewModel.delete(transaction)
            }
        )

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun observeTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            binding.tvEmptyState.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}