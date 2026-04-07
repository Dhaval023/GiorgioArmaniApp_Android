package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.statics.OutboundMainPageMenuModel

class OutboundMenuAdapter(
    private val onItemTap: (OutboundMainPageMenuModel) -> Unit
) : ListAdapter<OutboundMainPageMenuModel, OutboundMenuAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMenuTitle: TextView = itemView.findViewById(R.id.tvMenuTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outbound_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)

        holder.tvMenuTitle.text = model.title

        holder.itemView.setOnClickListener { onItemTap(model) }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<OutboundMainPageMenuModel>() {
            override fun areItemsTheSame(
                old: OutboundMainPageMenuModel,
                new: OutboundMainPageMenuModel
            ) = old.outboundMenuNavType == new.outboundMenuNavType

            override fun areContentsTheSame(
                old: OutboundMainPageMenuModel,
                new: OutboundMainPageMenuModel
            ) = old == new
        }
    }
}
