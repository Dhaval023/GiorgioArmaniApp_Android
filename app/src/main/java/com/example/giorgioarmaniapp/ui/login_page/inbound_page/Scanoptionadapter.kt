package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel

class ScanOptionAdapter(
    private val onOptionSelected: (ScanOptionModel) -> Unit
) : ListAdapter<ScanOptionModel, ScanOptionAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rbScanOption: RadioButton = itemView.findViewById(R.id.rbScanOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)

        holder.rbScanOption.text = model.title
        holder.rbScanOption.isChecked = model.isSelected

        // Since the RadioButton is inside a RecyclerView and we want to handle the selection logic in the ViewModel,
        // we intercept the click and pass it up.
        holder.rbScanOption.setOnClickListener {
            onOptionSelected(model)
        }
        
        holder.itemView.setOnClickListener {
            onOptionSelected(model)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ScanOptionModel>() {
            override fun areItemsTheSame(old: ScanOptionModel, new: ScanOptionModel) =
                old.id == new.id
            override fun areContentsTheSame(old: ScanOptionModel, new: ScanOptionModel) =
                old == new
        }
    }
}
