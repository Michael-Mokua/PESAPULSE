package com.pesapulse.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.data.repository.AppRepository
import com.pesapulse.databinding.FragmentTransactionsBinding
import com.pesapulse.ui.viewmodel.TransactionViewModel
import com.pesapulse.ui.viewmodel.ViewModelFactory

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(AppRepository(database.transactionDao(), database.goalDao()))
    }

    private val adapter = TransactionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTransactions.adapter = adapter

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
