package com.pesapulse.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pesapulse.R
import com.pesapulse.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("pesapulse_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_complete", false)) {
            findNavController().navigate(R.id.action_onboarding_to_dashboard)
        }

        binding.btnGetStarted.setOnClickListener {
            prefs.edit().putBoolean("onboarding_complete", true).apply()
            findNavController().navigate(R.id.action_onboarding_to_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
