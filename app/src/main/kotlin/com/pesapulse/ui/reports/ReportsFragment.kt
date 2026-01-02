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
            setupLineChart(transactions)
        }
    }

    private fun setupBarChart(transactions: List<com.pesapulse.data.model.TransactionEntity>) {
        val income = transactions.filter { it.type in listOf("received", "deposit") }.sumOf { it.amount }.toFloat()
        val expense = transactions.filter { it.type in listOf("sent", "payment", "withdrawal") }.sumOf { it.amount }.toFloat()

        val entries = listOf(
            BarEntry(0f, income),
            BarEntry(1f, expense)
        )
        val dataSet = BarDataSet(entries, "Income vs Expense")
        dataSet.colors = listOf(Color.GREEN, Color.RED)
        
        binding.incomeExpenseChart.data = BarData(dataSet)
        binding.incomeExpenseChart.description.isEnabled = false
        binding.incomeExpenseChart.invalidate()
    }

    private fun setupLineChart(transactions: List<com.pesapulse.data.model.TransactionEntity>) {
        if (transactions.isEmpty()) return
        
        val entries = transactions.asReversed().mapIndexed { index, tx ->
            Entry(index.toFloat(), tx.balance.toFloat())
        }
        val dataSet = LineDataSet(entries, "Balance Trend")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f

        binding.balanceTrendChart.data = LineData(dataSet)
        binding.balanceTrendChart.description.isEnabled = false
        binding.balanceTrendChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
