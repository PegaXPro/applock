package com.aviapp.app.security.applocker.ui.intruders

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ItemIntrudersPhotoBinding
import com.aviapp.app.security.applocker.ui.image_viewer.ImageScrActivity
import javax.inject.Inject

class IntrudersListAdapter @Inject constructor() :
    RecyclerView.Adapter<IntrudersListAdapter.IntrudersListItemViewHolder>() {

    private val intruderList = arrayListOf<IntruderPhotoItemViewState>()
    lateinit var viewModel: IntrudersPhotosViewModel

    fun updateIntruderList(intruderList: List<IntruderPhotoItemViewState>) {
        this.intruderList.clear()
        this.intruderList.addAll(intruderList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = intruderList.size

    override fun onBindViewHolder(holder: IntrudersListItemViewHolder, position: Int) =
        holder.bind(intruderList[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntrudersListItemViewHolder =
        IntrudersListItemViewHolder.create(parent, viewModel)

    class IntrudersListItemViewHolder(private val binding: ItemIntrudersPhotoBinding, val viewModel: IntrudersPhotosViewModel) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.squareLayoutItem.setOnClickListener {
            }
        }


        fun bind(intruderPhotoItemViewState: IntruderPhotoItemViewState) {

            binding.viewState = intruderPhotoItemViewState
            binding.executePendingBindings()

            binding.squareLayoutItem.setOnLongClickListener {
                DeleteDialog(binding.root.context) {
                    try {
                        intruderPhotoItemViewState.file.delete()
                        viewModel.loadIntruderPhotos()
                    } catch (e: Exception) { }
                }

                return@setOnLongClickListener true
            }

            binding.squareLayoutItem.setOnClickListener {
                itemView.context.startActivity(Intent(itemView.context, ImageScrActivity::class.java).also { it.putExtra("uri", intruderPhotoItemViewState.file.path) })
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                viewModel: IntrudersPhotosViewModel
            ): IntrudersListItemViewHolder {
                val binding: ItemIntrudersPhotoBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_intruders_photo,
                    parent,
                    false,
                )
                return IntrudersListItemViewHolder(binding, viewModel)

            }
        }

    }

}