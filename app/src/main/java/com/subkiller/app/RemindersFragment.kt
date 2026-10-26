package com.subkiller.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.subkiller.app.databinding.FragmentRemindersBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by activityViewModels()
    private lateinit var upcomingAdapter: SubscriptionAdapter
    private lateinit var freeTrialAdapter: SubscriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeReminders()
    }

    private fun setupRecyclerViews() {
        upcomingAdapter = SubscriptionAdapter(
            onPauseClick = { viewModel.togglePause(it) },
            onDeleteClick = { viewModel.deleteSubscription(it) }
        )
        binding.rvUpcomingPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingPayments.adapter = upcomingAdapter

        freeTrialAdapter = SubscriptionAdapter(
            onPauseClick = { viewModel.togglePause(it) },
            onDeleteClick = { viewModel.deleteSubscription(it) }
        )
        binding.rvFreeTrials.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFreeTrials.adapter = freeTrialAdapter
    }

    private fun observeReminders() {
        viewModel.allSubscriptions.observe(viewLifecycleOwner) { list ->
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            // Filter active subscriptions due in the next 7 days (0 <= days <= 7)
            val upcoming7Days = list.filter { sub ->
                if (sub.isPaused) return@filter false
                try {
                    val date = LocalDate.parse(sub.nextPaymentDate, formatter)
                    val days = ChronoUnit.DAYS.between(today, date)
                    days in 0..7
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { it.nextPaymentDate }

            upcomingAdapter.submitList(upcoming7Days)

            // Green Card: If no payments due this week, show "You're all caught up! No payments due this week."
            if (upcoming7Days.isEmpty()) {
                binding.cardAllCaughtUp.visibility = View.VISIBLE
                binding.rvUpcomingPayments.visibility = View.GONE
            } else {
                binding.cardAllCaughtUp.visibility = View.GONE
                binding.rvUpcomingPayments.visibility = View.VISIBLE
            }

            // Free Trials Ending Soon (within 7 days)
            val expiringTrials = list.filter { sub ->
                if (sub.isPaused || !sub.isFreeTrial) return@filter false
                try {
                    val date = LocalDate.parse(sub.nextPaymentDate, formatter)
                    val days = ChronoUnit.DAYS.between(today, date)
                    days in 0..7
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { it.nextPaymentDate }

            freeTrialAdapter.submitList(expiringTrials)
            binding.tvNoFreeTrials.visibility = if (expiringTrials.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
