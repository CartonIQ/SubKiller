package com.subkiller.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.subkiller.app.databinding.FragmentSettingsBinding
import com.subkiller.app.util.NotificationScheduler

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val PREFS_NAME = "subkiller_prefs"
    private val KEY_REMINDERS = "reminders_enabled"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrency()

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_REMINDERS, true)
        binding.switchReminders.isChecked = isEnabled

        // Switch: Enable Payment Reminders (uses WorkManager)
        binding.switchReminders.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_REMINDERS, checked).apply()
            if (checked) {
                NotificationScheduler.scheduleDailyReminder(requireContext())
                Toast.makeText(requireContext(), "Payment reminders scheduled for 9:00 AM daily", Toast.LENGTH_SHORT).show()
            } else {
                NotificationScheduler.cancelDailyReminder(requireContext())
                Toast.makeText(requireContext(), "Payment reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Button: Rate Us on Google Play
        binding.btnRateUs.setOnClickListener {
            val packageName = requireContext().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=\$packageName")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=\$packageName")))
            }
        }

        // Button: Privacy Policy (popup dialog)
        binding.btnPrivacyPolicy.setOnClickListener {
            showPrivacyPolicyDialog()
        }
    }

    private fun showPrivacyPolicyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.privacy_policy_title)
            .setMessage(R.string.privacy_policy_text)
            .setPositiveButton(R.string.btn_close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCurrency() {
        val prefs = requireContext().getSharedPreferences("subkiller_prefs", android.content.Context.MODE_PRIVATE)
        val currencies = arrayOf("₹ INR", "$ USD", "£ GBP", "€ EUR", "¥ JPY")
        val codes = arrayOf("INR", "USD", "GBP", "EUR", "JPY")
        
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencies)
        binding.spinnerCurrency.adapter = adapter
        
        val saved = prefs.getString("currency", "USD") ?: "USD"
        val idx = codes.indexOf(saved).coerceAtLeast(0)
        binding.spinnerCurrency.setSelection(idx)
        
        binding.spinnerCurrency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("currency", codes[pos]).apply()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

}
