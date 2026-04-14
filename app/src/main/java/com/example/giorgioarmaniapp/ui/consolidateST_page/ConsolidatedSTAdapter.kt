package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.OutBoundStockModel


class ConsolidatedSTAdapter(
    private val onDeleteClick: (OutBoundStockModel.OutBoundStockListModel) -> Unit
) : ListAdapter<OutBoundStockModel.OutBoundStockListModel, ConsolidatedSTAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OutBoundStockModel.OutBoundStockListModel>() {
            override fun areItemsTheSame(o: OutBoundStockModel.OutBoundStockListModel, n: OutBoundStockModel.OutBoundStockListModel) =
                o.globalTradeItemNumber == n.globalTradeItemNumber
            override fun areContentsTheSame(o: OutBoundStockModel.OutBoundStockListModel, n: OutBoundStockModel.OutBoundStockListModel) =
                o == n
        }
    }

     class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGTIN:       TextView = view.findViewById(R.id.tvGTIN)
        val tvActualQty:  TextView = view.findViewById(R.id.tvActualQty)
        val tvScannedQty: TextView = view.findViewById(R.id.tvScannedQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_consolidated_st, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvGTIN.text       = item.globalTradeItemNumber
        holder.tvActualQty.text  = item.actualQuantityDelivered.toString()
        holder.tvScannedQty.text = item.scannedQTY.toString()
    }

    fun attachSwipeToDelete(recyclerView: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(rv: RecyclerView,
                                vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = getItem(viewHolder.adapterPosition)
                if (item.isDelTag) {
                    onDeleteClick(item)
                } else {
                    notifyItemChanged(viewHolder.adapterPosition)
                }
            }
        }).attachToRecyclerView(recyclerView)
    }
}