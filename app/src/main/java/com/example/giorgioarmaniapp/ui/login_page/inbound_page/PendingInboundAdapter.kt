package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.InboundPendingListModel.InboundPendingListResult

class PendingInboundAdapter(
    private val onClick: (InboundPendingListResult) -> Unit
) : ListAdapter<InboundPendingListResult, PendingInboundAdapter.ViewHolder>(DiffUtil()) {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_inbound, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.view.findViewById<TextView>(R.id.txtInbound).text = item.deliveryNumber
        holder.view.findViewById<TextView>(R.id.txtOutbound).text = item.outboundNumber

        holder.view.setOnClickListener {
            onClick(item)
        }
    }

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<InboundPendingListResult>() {
        override fun areItemsTheSame(oldItem: InboundPendingListResult, newItem: InboundPendingListResult) = oldItem == newItem
        override fun areContentsTheSame(oldItem: InboundPendingListResult, newItem: InboundPendingListResult) = oldItem == newItem
    }
}