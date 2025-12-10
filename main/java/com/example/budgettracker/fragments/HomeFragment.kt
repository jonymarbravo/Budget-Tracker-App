//Package: fragments
//file name: HomeFragment

package com.example.budgettracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgettracker.databinding.FragmentHomeBinding
import com.example.budgettracker.utils.DateUtils
import com.example.budgettracker.viewmodels.TransactionViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        observeData()
    }

    private fun observeData() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncomeAmount.text = DateUtils.formatCurrency(income ?: 0.0)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpenseAmount.text = DateUtils.formatCurrency(expense ?: 0.0)
        }

        // Calculate balance
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
                val balance = (income ?: 0.0) - (expense ?: 0.0)
                binding.tvBalanceAmount.text = DateUtils.formatCurrency(balance)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}