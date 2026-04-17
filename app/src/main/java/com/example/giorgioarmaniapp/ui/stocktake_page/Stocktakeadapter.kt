package com.example.giorgioarmaniapp.ui.login_page.stocktake_page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.StockTakeModel

class StockTakeAdapter(
    private val onDeleteClick: (StockTakeModel.StockTakeListModel) -> Unit
) : ListAdapter<StockTakeModel.StockTakeListModel, StockTakeAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StockTakeModel.StockTakeListModel>() {
            override fun areItemsTheSame(old: StockTakeModel.StockTakeListModel, new: StockTakeModel.StockTakeListModel) =
                old.globalTradeItemNumber == new.globalTradeItemNumber

            override fun areContentsTheSame(old: StockTakeModel.StockTakeListModel, new: StockTakeModel.StockTakeListModel) =
                old == new
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProductId: TextView  = view.findViewById(R.id.tvProductId)
        val tvSOHQty:    TextView  = view.findViewById(R.id.tvSOHQty)
        val tvScannedQty:TextView  = view.findViewById(R.id.tvScannedQty)
        val btnDelete:   ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_take, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvProductId.text   = item.globalTradeItemNumber
        holder.tvSOHQty.text      = item.sohQuantity.toString()
        holder.tvScannedQty.text  = item.scannedQTY.toString()

        val diff = item.scannedQTY - item.sohQuantity
        holder.itemView.setBackgroundColor(
            when {
                diff < 0 -> Color.parseColor("#FFCCCC")  // under-scanned
                diff > 0 -> Color.parseColor("#CCFFCC")  // over-scanned
                else     -> Color.TRANSPARENT
            }
        )

        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }
}
