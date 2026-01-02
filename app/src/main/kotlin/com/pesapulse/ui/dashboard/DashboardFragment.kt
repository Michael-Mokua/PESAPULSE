package com.pesapulse.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.data.repository.AppRepository
import com.pesapulse.databinding.FragmentDashboardBinding
import com.pesapulse.ui.transactions.TransactionAdapter
import com.pesapulse.ui.viewmodel.TransactionViewModel
import com.pesapulse.ui.viewmodel.ViewModelFactory
import com.pesapulse.util.FinancialAdvisor

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(AppRepository(database.transactionDao(), database.goalDao()))
    }

    private val adapter = TransactionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvRecentTransactions.adapter = adapter

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.latestTransaction.observe(viewLifecycleOwner) { tx ->
            tx?.let {
                binding.tvBalance.text = "KES ${String.format("%.2f", it.balance)}"
            }
        }

        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "KES ${String.format("%.2f", income ?: 0.0)}"
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "KES ${String.format("%.2f", expense ?: 0.0)}"
            updateAdvice()
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions.take(10))
            setupChart(transactions)
        }
    }

    private fun updateAdvice() {
        val balanceStr = binding.tvBalance.text.toString().replace("KES ", "").replace(",", "")
        val balance = balanceStr.toDoubleOrNull() ?: 0.0
        val income = viewModel.monthlyIncome.value ?: 0.0
        val expense = viewModel.monthlyExpenses.value ?: 0.0
        
        binding.tvAdvice.text = FinancialAdvisor.getAdvice(balance, income, expense)
    }

    private fun setupChart(transactions: List<com.pesapulse.data.model.TransactionEntity>) {
        val categories = FinancialAdvisor.categorizeSpending(transactions)
        if (categories.isEmpty()) return

        val entries = categories.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Spending by Category")
        dataSet.colors = listOf(Color.GREEN, Color.BLUE, Color.MAGENTA, Color.YELLOW, Color.CYAN)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        binding.spending_chart.data = PieData(dataSet)
        binding.spending_chart.centerText = "Expenses"
        binding.spending_chart.description.isEnabled = false
        binding.spending_chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
