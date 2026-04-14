package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.OutBoundStockModel

class PendingOutboundAdapter(
    private var list: List<OutBoundStockModel.PendingOutboundResult>,
    private val onClick: (OutBoundStockModel.PendingOutboundResult) -> Unit
) : RecyclerView.Adapter<PendingOutboundAdapter.ViewHolder>() {
    
    fun updateData(newList: List<OutBoundStockModel.PendingOutboundResult>) {
        list = newList
        notifyDataSetChanged()
    }
     class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStore: TextView = view.findViewById(R.id.tvStore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_outbound, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvStore.text = item.toStore

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }
}