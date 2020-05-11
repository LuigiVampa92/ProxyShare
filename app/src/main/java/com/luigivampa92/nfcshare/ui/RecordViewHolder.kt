package com.luigivampa92.nfcshare.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.luigivampa92.nfcshare.R
import com.luigivampa92.nfcshare.Record
import com.luigivampa92.nfcshare.setVisibility

class RecordViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
    private val onItemClickListener: ((Record) -> Unit)? = null,
    private val onPushButtonClickListener: ((Record) -> Unit)? = null
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_record, container, false)) {

    val viewForeground: ConstraintLayout
    private val textTitle: TextView
    private val textDescription: TextView
    private val imageCheck: ImageView
    private val buttonNdefPush: MaterialButton

    init {
        viewForeground = itemView.findViewById(R.id.view_foreground)
        textTitle = itemView.findViewById(R.id.text_record_title)
        textDescription = itemView.findViewById(R.id.text_record_description)
        imageCheck = itemView.findViewById(R.id.img_check)
        buttonNdefPush = itemView.findViewById(R.id.button_push)
    }

    fun bind(record: Record) {
        imageCheck.setVisibility(record.selectedForHce)
        buttonNdefPush.setVisibility(false)

        textTitle.text = record.value.address
        textDescription.text = null

        viewForeground.setOnClickListener {
            onItemClickListener?.invoke(record)
        }
        buttonNdefPush.setOnClickListener {
            onPushButtonClickListener?.invoke(record)
        }
    }
}