package com.hebaelsaid.android.merchantapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hebaelsaid.android.merchantapp.R
import com.hebaelsaid.android.merchantapp.data.OrderDataModel

class OrdersListAdapter  (private val items: List<OrderDataModel>)
    : RecyclerView.Adapter<OrdersListAdapter.OrdersListViewHolder>() {

    private lateinit var context: Context
    var listener: OnItemClickListener?=null
    fun setOnClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersListViewHolder {
        context = parent.context
        return OrdersListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.orders_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersListViewHolder, position: Int) {
        holder.bind(items[position].orderID.toString())
        holder.acceptOrderBtn.setOnClickListener {
            listener!!.onClick(items[position].orderID.toString(),true)
        }
        holder.rejectOrderBtn.setOnClickListener {
            listener!!.onClick(items[position].orderID.toString(),false)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class OrdersListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val orderID = view.findViewById<TextView>(R.id.order_counter_number)
        val acceptOrderBtn = view.findViewById<Button>(R.id.accept_order)
        val rejectOrderBtn = view.findViewById<Button>(R.id.reject_order)

        @SuppressLint("SetTextI18n")
        fun bind(item:String){
            orderID.text = "Order No. $item"
        }

    }
    interface OnItemClickListener{
        fun onClick(orderID:String,isAccepted:Boolean)
    }

}
