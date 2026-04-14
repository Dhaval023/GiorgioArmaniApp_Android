package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.OutBoundStockModel

class STItemAdapter(
    private val onDelete: (OutBoundStockModel.OutBoundStockListModel, Int) -> Unit
) : ListAdapter<OutBoundStockModel.OutBoundStockListModel, STItemAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGTIN: TextView = itemView.findViewById(R.id.tvGTIN)
        val tvScannedQTY: TextView = itemView.findViewById(R.id.tvScannedQTY)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stocktransfer_item_outbound, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvGTIN.text = item.globalTradeItemNumber
        holder.tvScannedQTY.text = item.scannedQTY.toString()
    }

    fun attachSwipeTo(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position)
                if (item.isDelTag) {
                    onDelete(item, position)
                } else {
                    notifyItemChanged(position)
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val itemView = viewHolder.itemView
                    val paint = Paint().apply { color = Color.RED }
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                    )

                    val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.bin)
                    if (icon != null) {
                        val size = 45
                        val iconMargin = (itemView.height - size) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + size
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - size
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<OutBoundStockModel.OutBoundStockListModel>() {
                override fun areItemsTheSame(
                    old: OutBoundStockModel.OutBoundStockListModel,
                    new: OutBoundStockModel.OutBoundStockListModel
                ) = old.globalTradeItemNumber == new.globalTradeItemNumber

                override fun areContentsTheSame(
                    old: OutBoundStockModel.OutBoundStockListModel,
                    new: OutBoundStockModel.OutBoundStockListModel
                ) = old.globalTradeItemNumber == new.globalTradeItemNumber
                        && old.scannedQTY == new.scannedQTY
                        && old.actualQuantityDelivered == new.actualQuantityDelivered
                        && old.isDelTag == new.isDelTag
            }
    }
}