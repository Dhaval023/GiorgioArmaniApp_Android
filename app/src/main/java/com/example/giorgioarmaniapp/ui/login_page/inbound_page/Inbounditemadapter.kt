package com.example.giorgioarmaniapp.ui.login_page.inbound_page


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.InboundPendingListModel

class InboundItemAdapter(
    private val onDelete: (InboundPendingListModel.InboundPendingModel) -> Unit,
    private val onMakeEqual: (InboundPendingListModel.InboundPendingModel) -> Unit
) : ListAdapter<InboundPendingListModel.InboundPendingModel, InboundItemAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGTIN: TextView = itemView.findViewById(R.id.tvGTIN)
        val tvExpectedQTY: TextView = itemView.findViewById(R.id.tvExpectedQTY)
        val tvScannedQTY: TextView = itemView.findViewById(R.id.tvScannedQTY)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbound, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // Bind columns — originally Grid columns bound to InvalidGTINNumberCLR
        val textColorStr = item.invalidGTINNumberCLR ?: "#000000"
        val textColor = try { Color.parseColor(textColorStr) } catch (e: Exception) { Color.BLACK }

        holder.tvGTIN.apply {
            text = item.globalTradeItemNumber
            setTextColor(textColor)
        }
        holder.tvExpectedQTY.apply {
            text = item.actualQuantityDelivered.toString()
            setTextColor(textColor)
        }
        holder.tvScannedQTY.apply {
            text = item.scannedQTY.toString()
            setTextColor(textColor)
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<InboundPendingListModel.InboundPendingModel>() {
                override fun areItemsTheSame(
                    old: InboundPendingListModel.InboundPendingModel,
                    new: InboundPendingListModel.InboundPendingModel
                ) = old.globalTradeItemNumber == new.globalTradeItemNumber

                override fun areContentsTheSame(
                    old: InboundPendingListModel.InboundPendingModel,
                    new: InboundPendingListModel.InboundPendingModel
                ) = old == new
            }
    }

    // region --- Swipe-reveal ItemTouchHelper ---

    fun attachSwipeTo(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                rv: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position)

                when {
                    item.isDelTag ->
                        onDelete(item)
                    item.isInvalidCount ->
                        onMakeEqual(item)
                    else ->
                        notifyItemChanged(position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val item = getItem(position)

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val itemView = viewHolder.itemView
                    val paint = Paint().apply { color = Color.RED }
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(),
                        paint
                    )
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    // endregion
}