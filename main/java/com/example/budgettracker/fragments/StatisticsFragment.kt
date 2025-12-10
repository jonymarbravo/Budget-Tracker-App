//Package: fragments
//file name: StatisticsFragment.kt

package com.example.budgettracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgettracker.databinding.FragmentStatisticsBinding
import com.example.budgettracker.models.TransactionType
import com.example.budgettracker.utils.DateUtils
import com.example.budgettracker.viewmodels.TransactionViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        setupPieChart()
        observeData()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            // Disable description
            description.isEnabled = false

            // Enable hole in the middle
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 45f
            transparentCircleRadius = 50f

            // Center text
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(16f)
            setCenterTextColor(Color.parseColor("#333333"))

            // Rotation and animation
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // Disable default legend - we'll create custom one
            legend.isEnabled = false

            // Entry label styling - ENABLE labels on slices
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
            setDrawEntryLabels(true) // ENABLE to show category names on slices

            // Touch and selection
            setTouchEnabled(true)

            // No spin animation initially
            spin(0, 0f, 0f, Easing.Linear)
        }
    }

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

            if (expenseTransactions.isEmpty()) {
                binding.emptyStateCard.visibility = View.VISIBLE
                binding.chartCard.visibility = View.GONE
                binding.legendCard.visibility = View.GONE
                return@observe
            }

            binding.emptyStateCard.visibility = View.GONE
            binding.chartCard.visibility = View.VISIBLE
            binding.legendCard.visibility = View.VISIBLE

            // Group by category and calculate totals
            val categoryTotals = expenseTransactions
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            // Create pie entries
            val entries = categoryTotals.map {
                PieEntry(it.value.toFloat(), it.key)
            }

            // Create custom value formatter for currency
            val currencyFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return DateUtils.formatCurrency(value.toDouble())
                }
            }

            // Color list
            val colors = listOf(
                Color.parseColor("#FF6B6B"), // Red
                Color.parseColor("#4ECDC4"), // Teal
                Color.parseColor("#45B7D1"), // Blue
                Color.parseColor("#FFA07A"), // Light Salmon
                Color.parseColor("#98D8C8"), // Mint
                Color.parseColor("#F7DC6F"), // Yellow
                Color.parseColor("#BB8FCE"), // Purple
                Color.parseColor("#85C1E2"), // Sky Blue
                Color.parseColor("#F8B88B"), // Peach
                Color.parseColor("#A8E6CF"), // Light Green
                Color.parseColor("#FFD3B6"), // Cream
                Color.parseColor("#FFAAA5"), // Pink
                Color.parseColor("#A8E6CE"), // Pastel Green
                Color.parseColor("#DCEDC2"), // Lime
                Color.parseColor("#FFD5CD"), // Light Pink
                Color.parseColor("#C7CEEA")  // Lavender
            )

            // Create dataset with beautiful colors
            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors

                // Slice styling
                sliceSpace = 2f
                selectionShift = 8f

                // Value styling - Use currency formatter
                valueTextSize = 11f
                valueTextColor = Color.parseColor("#333333")
                valueFormatter = currencyFormatter

                // Draw values outside with lines
                valueLinePart1OffsetPercentage = 80f
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.6f
                valueLineColor = Color.parseColor("#666666")
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            }

            // Create pie data
            val data = PieData(dataSet).apply {
                setValueTextSize(11f)
                setValueTextColor(Color.parseColor("#333333"))
                setValueFormatter(currencyFormatter)
            }

            // Set data and animate
            binding.pieChart.data = data
            binding.pieChart.highlightValues(null)
            binding.pieChart.animateY(1000, Easing.EaseInOutQuad)

            // Create custom legend with flex-wrap layout
            createCustomLegend(categoryTotals, colors)

            binding.pieChart.invalidate()
        }
    }

    private fun createCustomLegend(categoryTotals: Map<String, Double>, colors: List<Int>) {
        binding.legendContainer.removeAllViews()

        Log.d("StatisticsFragment", "Creating legend with ${categoryTotals.size} categories")

        var currentRow: LinearLayout? = null

        categoryTotals.entries.forEachIndexed { index, entry ->
            Log.d("StatisticsFragment", "Adding category: ${entry.key} with amount: ${entry.value}")

            // Create new row every 2 items
            if (index % 2 == 0) {
                currentRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val dp = resources.displayMetrics.density.toInt()
                    setPadding(0, 0, 0, 12 * dp)
                }
                binding.legendContainer.addView(currentRow)
            }

            // Create legend item container
            val itemContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL

                val params = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                val dp = resources.displayMetrics.density.toInt()
                params.setMargins(0, 0, 12 * dp, 0)
                layoutParams = params
            }

            // Create color box with rounded corners
            val dp = resources.displayMetrics.density
            val boxSize = (20 * dp).toInt()

            val colorBox = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(boxSize, boxSize).apply {
                    setMargins(0, 0, (8 * dp).toInt(), 0)
                }

                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.setColor(colors[index % colors.size])
                drawable.cornerRadius = 4f * dp
                background = drawable
            }

            // Create text view for category name
            val textView = TextView(requireContext()).apply {
                text = entry.key
                textSize = 13f
                setTextColor(Color.parseColor("#666666"))
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Add views to item container
            itemContainer.addView(colorBox)
            itemContainer.addView(textView)

            // Add item container to current row
            currentRow?.addView(itemContainer)

            Log.d("StatisticsFragment", "Added ${entry.key} to legend")
        }

        Log.d("StatisticsFragment", "Legend creation complete")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}