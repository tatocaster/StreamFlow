package com.lukakordzaia.streamflow.ui.phone.sharedadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lukakordzaia.streamflow.R
import com.lukakordzaia.streamflow.databinding.RvSingleGenreItemBinding
import com.lukakordzaia.streamflow.datamodels.SingleTitleModel
import com.lukakordzaia.streamflow.network.models.imovies.response.titles.GetTitlesResponse
import com.lukakordzaia.streamflow.utils.setImage
import com.squareup.picasso.Picasso

class SingleCategoryAdapter(private val context: Context, private val onTitleClick: (id : Int) -> Unit) : RecyclerView.Adapter<SingleCategoryAdapter.ViewHolder>() {
    private var list: List<SingleTitleModel> = ArrayList()
    private var startPosition = 0

    fun setItems(list: List<SingleTitleModel>) {
        this.list = list
        startPosition += list.size
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvSingleGenreItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listModel = list[position]

        holder.bind(listModel)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val view: RvSingleGenreItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(model: SingleTitleModel) {
            view.itemName.text = model.displayName
            view.isTvShow.text = if (model.isTvShow) "სერიალი" else "ფილმი"

            view.itemPoster.setImage(model.poster, true)

            view.root.setOnClickListener {
                onTitleClick(model.id)
                it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
            }
        }
    }
}