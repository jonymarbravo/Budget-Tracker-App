//Package: activities
//file name: AddTransactionActivity.kt

package com.example.budgettracker.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgettracker.databinding.ActivityAddTransactionBinding
import com.example.budgettracker.models.Transaction
import com.example.budgettracker.models.TransactionType
import com.example.budgettracker.utils.Categories
import com.example.budgettracker.utils.DateUtils
import com.example.budgettracker.viewmodels.TransactionViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: TransactionViewModel
    private var selectedDate: Long = System.currentTimeMillis()
    private var transactionId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        setupToolbar()
        setupTransactionTypeListener()
        setupDatePicker()
        setupSaveButton()

        // Check if editing existing transaction
        transactionId = intent.getLongExtra("transaction_id", -1L)
        if (transactionId != -1L) {
            loadTransaction(transactionId)
        } else {
            // For new transaction, set default category
            updateCategorySpinner()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (transactionId != -1L) "Edit Transaction" else "Add Transaction"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTransactionTypeListener() {
        binding.chipIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner()
        }
        binding.chipExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner()
        }
    }

    private fun updateCategorySpinner() {
        val categories = if (binding.chipIncome.isChecked) {
            Categories.incomeCategories
        } else {
            Categories.expenseCategories
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)

        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun loadTransaction(id: Long) {
        lifecycleScope.launch {
            try {
                viewModel.getTransactionById(id)?.let { transaction ->
                    // Set transaction type
                    if (transaction.type == TransactionType.INCOME) {
                        binding.chipIncome.isChecked = true
                    } else {
                        binding.chipExpense.isChecked = true
                    }

                    // Set fields
                    binding.etTitle.setText(transaction.title)
                    binding.etAmount.setText(transaction.amount.toString())
                    binding.etNote.setText(transaction.note)
                    selectedDate = transaction.date
                    binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)

                    // Set category
                    updateCategorySpinner()
                    val categories = if (transaction.type == TransactionType.INCOME) {
                        Categories.incomeCategories
                    } else {
                        Categories.expenseCategories
                    }
                    val categoryIndex = categories.indexOf(transaction.category)
                    if (categoryIndex != -1) {
                        binding.spinnerCategory.setSelection(categoryIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AddTransactionActivity, "Error loading transaction", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
        val note = binding.etNote.text.toString().trim()

        // Validation
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            binding.etTitle.requestFocus()
            return
        }

        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            binding.etAmount.requestFocus()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter valid amount"
            binding.etAmount.requestFocus()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (binding.chipIncome.isChecked) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        val transaction = Transaction(
            id = if (transactionId != -1L) transactionId else 0,
            title = title,
            amount = amount,
            type = type,
            category = category,
            note = note,
            date = selectedDate
        )

        if (transactionId != -1L) {
            viewModel.update(transaction)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(transaction)
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}