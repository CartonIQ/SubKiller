package com.subkiller.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.subkiller.app.data.SubscriptionEntity
import com.subkiller.app.databinding.ItemSubscriptionBinding
import java.util.Locale

class SubscriptionAdapter(
    private val onPauseClick: (SubscriptionEntity) -> Unit,
    private val onDeleteClick: (SubscriptionEntity) -> Unit
) : ListAdapter<SubscriptionEntity, SubscriptionAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubscriptionEntity) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.tvDate.text = "Next charge: ${item.nextPaymentDate}"
            binding.tvPrice.text = String.format(Locale.US, "${getCurrencySymbolForAdapter(binding.root.context)}%.2f", item.price)

            // Free Trial Tag
            binding.tvTrialBadge.visibility = if (item.isFreeTrial) View.VISIBLE else View.GONE

            // Paused state styling
            if (item.isPaused) {
                binding.cardSubscription.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.paused_card_bg)
                )
                binding.cardSubscription.alpha = 0.55f
                binding.tvPausedBadge.visibility = View.VISIBLE
                binding.btnPause.text = binding.root.context.getString(R.string.action_resume)
                binding.btnPause.setIconResource(R.drawable.ic_play)
                binding.btnPause.setIconTintResource(R.color.primary_neon_green)
                binding.btnPause.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.primary_neon_green)
                )
            } else {
                binding.cardSubscription.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.card_dark)
                )
                binding.cardSubscription.alpha = 1.0f
                binding.tvPausedBadge.visibility = View.GONE
                binding.btnPause.text = binding.root.context.getString(R.string.action_pause)
                binding.btnPause.setIconResource(R.drawable.ic_pause)
                binding.btnPause.setIconTintResource(R.color.text_secondary)
                binding.btnPause.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.text_secondary)
                )
            }

            binding.btnPause.setOnClickListener { onPauseClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SubscriptionEntity>() {
        override fun areItemsTheSame(oldItem: SubscriptionEntity, newItem: SubscriptionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubscriptionEntity, newItem: SubscriptionEntity): Boolean {
            return oldItem == newItem
        }
    }


    private fun getCurrencySymbolForAdapter(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("subkiller_prefs", android.content.Context.MODE_PRIVATE)
        return when (prefs.getString("currency", "USD")) {
            "INR" -> "₹"
            "GBP" -> "£"
            "EUR" -> "€"
            "JPY" -> "¥"
            else -> "$"
        }
    }

}
