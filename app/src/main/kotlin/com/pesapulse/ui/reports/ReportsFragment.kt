package com.pesapulse.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.*
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.data.repository.AppRepository
import com.pesapulse.databinding.FragmentReportsBinding
import com.pesapulse.ui.viewmodel.TransactionViewModel
import com.pesapulse.ui.viewmodel.ViewModelFactory

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(AppRepository(database.transactionDao(), database.goalDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            setupBarChart(transactions)
            setupTrendChart(transactions)
        }
    }

    private fun setupBarChart(transactions: List<com.pesapulse.data.model.TransactionEntity>) {
        val income = transactions.filter { it.type in listOf("received", "deposit") }
            .map { it.amount }.fold(java.math.BigDecimal.ZERO) { acc, amt -> acc.add(amt) }.toFloat()
        val expense = transactions.filter { it.type in listOf("sent", "payment", "withdrawal") }
            .map { it.amount }.fold(java.math.BigDecimal.ZERO) { acc, amt -> acc.add(amt) }.toFloat()

        val entries = listOf(
            BarEntry(1f, income ?: 0.0f),
            BarEntry(2f, expense ?: 0.0f)
        )

        val dataSet = BarDataSet(entries, "Income vs Expense").apply {
            colors = listOf(Color.parseColor("#00FF88"), Color.parseColor("#FF3D00"))
            valueTextColor = Color.WHITE
        }

        binding.incomeExpenseChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    private fun setupTrendChart(transactions: List<com.pesapulse.data.model.TransactionEntity>) {
        // Trend chart logic with neon colors
        val entries = transactions.reversed().take(10).mapIndexed { index, tx -> 
            Entry(index.toFloat(), tx.balance.toFloat()) 
        }

        val dataSet = LineDataSet(entries, "Balance Trend").apply {
            color = Color.parseColor("#00E5FF")
            setDrawCircles(true)
            setCircleColor(Color.WHITE)
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#00E5FF")
            fillAlpha = 30
        }

        binding.balanceTrendChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
