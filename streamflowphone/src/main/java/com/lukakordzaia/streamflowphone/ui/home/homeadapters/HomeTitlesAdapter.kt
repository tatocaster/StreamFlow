package com.lukakordzaia.streamflowphone.ui.home.homeadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lukakordzaia.core.datamodels.SingleTitleModel
import com.lukakordzaia.core.utils.setImage
import com.lukakordzaia.streamflowphone.databinding.RvHomeItemBinding

class HomeTitlesAdapter(private val onTitleClick: (id: Int) -> Unit) : RecyclerView.Adapter<HomeTitlesAdapter.ViewHolder>() {
    private var list: List<SingleTitleModel> = ArrayList()

    fun setItems(list: List<SingleTitleModel>) {
        this.list = list
        notifyItemRangeInserted(0, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvHomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listModel = list[position]

        holder.bind(listModel)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val view: RvHomeItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(model: SingleTitleModel) {
            view.itemName.text = model.displayName
            view.itemPoster.setImage(model.poster, true)

            view.root.setOnClickListener {
                onTitleClick(model.id)
            }
        }
    }

}