package com.pesapulse.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.data.model.GoalEntity
import com.pesapulse.data.repository.AppRepository
import com.pesapulse.databinding.FragmentSettingsBinding
import com.pesapulse.ui.viewmodel.TransactionViewModel
import com.pesapulse.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(AppRepository(database.transactionDao(), database.goalDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        binding.btnExportCsv.setOnClickListener {
            exportData()
        }
    }

    private fun saveGoal() {
        val name = binding.etGoalName.text.toString()
        val amountStr = binding.etGoalAmount.text.toString()
        val amount = try { java.math.BigDecimal(amountStr) } catch (e: Exception) { java.math.BigDecimal.ZERO }

        if (name.isNotEmpty() && amount > java.math.BigDecimal.ZERO) {
            val database = AppDatabase.getDatabase(requireContext())
            CoroutineScope(Dispatchers.IO).launch {
                database.goalDao().insertGoal(GoalEntity(name = name, targetAmount = amount, currentAmount = java.math.BigDecimal.ZERO, deadline = null))
            }
            Toast.makeText(context, "Goal saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportData() {
        // Implementation for CSV export
        Toast.makeText(context, "Exporting data to CSV...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
