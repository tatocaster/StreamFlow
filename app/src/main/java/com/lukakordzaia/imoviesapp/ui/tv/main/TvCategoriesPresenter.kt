package com.lukakordzaia.imoviesapp.ui.tv.main

import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.lukakordzaia.imoviesapp.R
import com.lukakordzaia.imoviesapp.datamodels.TvCategoriesList
import com.lukakordzaia.imoviesapp.ui.customviews.TvCategoriesCardView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.tv_categories_card_view.view.*

class TvCategoriesPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = TvCategoriesCardView(parent.context, null)

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val categories = item as TvCategoriesList
        val cardView = viewHolder.view as TvCategoriesCardView

        cardView.tv_categories_card_title.text = categories.categoriesTitle
        Picasso.get().load(categories.categoriesIcon).error(categories.categoriesIcon).into(cardView.tv_categories_card_poster)

    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}