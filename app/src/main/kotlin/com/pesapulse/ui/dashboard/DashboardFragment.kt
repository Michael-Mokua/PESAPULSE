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
import com.pesapulse.util.HistoricalSmsImporter
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import kotlinx.coroutines.launch

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

        binding.btnResync.setOnClickListener {
            binding.btnResync.animate().rotationBy(360f).setDuration(1000).start()
            importHistory()
        }
    }

    private fun importHistory() {
        val database = AppDatabase.getDatabase(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            HistoricalSmsImporter.importLast6Months(requireContext(), database)
            Toast.makeText(requireContext(), "Pulse Synced", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.latestTransaction.observe(viewLifecycleOwner) { tx ->
            tx?.let {
                binding.tvBalance.text = "KES ${formatCurrency(it.balance)}"
                binding.tvFulizaBal.text = "KES ${formatCurrency(it.fulizaBalance)}"
                binding.tvFulizaLimit.text = "KES ${formatCurrency(it.fulizaLimit)}"
                
                // Calculate Pulse Score dynamically
                val score = calculatePulseScore(it.balance, it.fulizaBalance)
                binding.pulseGauge.setScore(score)
            }
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions.take(15))
            setupPredictionChart(transactions)
            detectSubscriptions(transactions)
            showFrequentContacts(transactions)
        }
    }

    private fun formatCurrency(amount: java.math.BigDecimal): String {
        val df = java.text.DecimalFormat("#,##0.00")
        return df.format(amount)
    }

    private fun showFrequentContacts(history: List<com.pesapulse.data.model.TransactionEntity>) {
        val contacts = com.pesapulse.util.AiFinancialCore.getFrequentCounterparties(history)
        binding.tvFrequentContacts.text = if (contacts.isEmpty()) "No frequent contacts yet."
            else contacts.joinToString("\n") { (name, flow, count) -> 
                "$name • $count tx • ${if (flow >= java.math.BigDecimal.ZERO) "+" else ""}${formatCurrency(flow)}"
            }
    }

    private fun detectSubscriptions(history: List<com.pesapulse.data.model.TransactionEntity>) {
        val subs = com.pesapulse.util.AiFinancialCore.detectSubscriptions(history)
        binding.tvDetectedSubs.text = if (subs.isEmpty()) "No recurring subs found yet." 
            else subs.joinToString(", ")
    }

    private fun setupPredictionChart(history: List<com.pesapulse.data.model.TransactionEntity>) {
        val predictions = com.pesapulse.util.AiFinancialCore.predictBalance(history)
        if (predictions.isEmpty()) return

        val entries = predictions.mapIndexed { index, pair -> 
            com.github.mikephil.charting.data.Entry(index.toFloat(), pair.second.toFloat()) 
        }
// ... rest of setupPredictionChart logic is same, just needed the map update ...
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Predicted Balance").apply {
            color = Color.parseColor("#00E5FF")
            setDrawCircles(false)
            lineWidth = 3f
            mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#00E5FF")
            fillAlpha = 50
        }

        binding.chartPrediction.apply {
            data = com.github.mikephil.charting.data.LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.textColor = Color.WHITE
            setTouchEnabled(false)
            invalidate()
        }
    }

    private fun calculatePulseScore(balance: java.math.BigDecimal, debt: java.math.BigDecimal): Int {
        val net = balance.subtract(debt)
        val ksh500 = java.math.BigDecimal("500")
        val ksh2000 = java.math.BigDecimal("2000")
        val ksh10000 = java.math.BigDecimal("10000")

        return when {
            net <= java.math.BigDecimal.ZERO -> 300
            net < ksh500 -> 450
            net < ksh2000 -> 600
            net < ksh10000 -> 800
            else -> 950
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
