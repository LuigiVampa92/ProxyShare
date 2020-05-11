package com.luigivampa92.nfcshare.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.nfcshare.Record

class RecordAdapter (
    private val onItemClickListener: ((Record) -> Unit)? = null,
    private val onPushButtonClickListener: ((Record) -> Unit)? = null
) : RecyclerView.Adapter<RecordViewHolder>() {

    private val items: ArrayList<Record> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecordViewHolder(LayoutInflater.from(parent.context), parent, onItemClickListener, onPushButtonClickListener)

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setRecords(records: List<Record>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }

    fun changeRecordHceEnabled(record: Record) {
        val index = items.indexOfFirst { it == record }
        val selectedForHce = items[index].selectedForHce
        for (i in 0 until items.size) {
            items[i] = items[i].copy(selectedForHce = if (i == index) !selectedForHce else false)
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Record = items[position]

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}