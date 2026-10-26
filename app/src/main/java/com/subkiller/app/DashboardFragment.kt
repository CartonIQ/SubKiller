package com.subkiller.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.subkiller.app.ads.AdManager
import com.subkiller.app.data.SubscriptionEntity
import com.subkiller.app.databinding.DialogAddSubscriptionBinding
import com.subkiller.app.databinding.FragmentDashboardBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by activityViewModels()
    private lateinit var adapter: SubscriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToggle()
        setupFab()
        observeData()

        // Load banner ad at bottom of Dashboard
        AdManager.loadBanner(requireContext(), binding.bannerAdContainer)

        // Show interstitial ad every 3rd time user opens Dashboard
        activity?.let { AdManager.onDashboardOpened(it) }
    }

    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter(
            onPauseClick = { subscription -> viewModel.togglePause(subscription) },
            onDeleteClick = { subscription -> showDeleteConfirmation(subscription) }
        )
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubscriptions.adapter = adapter
    }

    private fun setupToggle() {
        binding.togglePeriod.check(R.id.btn_monthly)
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isYearly = checkedId == R.id.btn_yearly
                viewModel.setYearlyToggle(isYearly)
                binding.tvTotalCostLabel.text = if (isYearly) {
                    getString(R.string.total_yearly_cost)
                } else {
                    getString(R.string.total_monthly_cost)
                }
                updateCostDisplay()
            }
        }
    }

    private fun setupFab() {
        binding.fabAddSubscription.setOnClickListener {
            showAddSubscriptionDialog()
        }
    }

    private fun observeData() {
        viewModel.allSubscriptions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            val activeCount = list.count { !it.isPaused }
            binding.tvActiveCount.text = "$activeCount Active"
            updateCostDisplay()
        }

        viewModel.isYearly.observe(viewLifecycleOwner) {
            updateCostDisplay()
        }
    }

    private fun updateCostDisplay() {
        val list = viewModel.allSubscriptions.value ?: emptyList()
        val monthlySum = list.filter { !it.isPaused }.sumOf { it.price }
        val isYearly = viewModel.isYearly.value == true
        val amount = if (isYearly) monthlySum * 12 else monthlySum
        binding.tvTotalCostAmount.text = String.format(Locale.US, "${getCurrencySymbol()}%.2f", amount)
    }

    private fun showAddSubscriptionDialog() {
        val dialogBinding = DialogAddSubscriptionBinding.inflate(layoutInflater)
        dialogBinding.etPrice.hint = "Price (${getCurrencySymbol()})"
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        dialogBinding.etDate.setText(selectedDate)

        dialogBinding.etDate.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = LocalDate.of(year, month + 1, dayOfMonth)
                    selectedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    dialogBinding.etDate.setText(selectedDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.btnDialogCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnDialogSave.setOnClickListener {
            val name = dialogBinding.etName.text?.toString()?.trim().orEmpty()
            val priceStr = dialogBinding.etPrice.text?.toString()?.trim().orEmpty()
            val category = dialogBinding.etCategory.text?.toString()?.trim().orEmpty().ifEmpty { "General" }
            val isTrial = dialogBinding.cbFreeTrial.isChecked

            val price = priceStr.toDoubleOrNull()
            if (name.isEmpty() || price == null || price < 0) {
                Toast.makeText(requireContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addSubscription(name, price, selectedDate, category, isTrial)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(subscription: SubscriptionEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete ${subscription.name}?")
            .setMessage("Are you sure you want to delete this subscription? This cannot be undone.")
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteSubscription(subscription)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getCurrencySymbol(): String {
        val prefs = requireContext().getSharedPreferences("subkiller_prefs", android.content.Context.MODE_PRIVATE)
        return when (prefs.getString("currency", "USD")) {
            "INR" -> "₹"
            "GBP" -> "£"
            "EUR" -> "€"
            "JPY" -> "¥"
            else -> "$"
        }
    }

}
