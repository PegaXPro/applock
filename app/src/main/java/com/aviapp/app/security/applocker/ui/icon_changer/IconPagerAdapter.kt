package com.aviapp.app.security.applocker.ui.icon_changer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.app.security.applocker.R

class IconPagerAdapter(
    private var checkedBox: MutableList<Boolean>,
    private var titles: List<Int>,
    private var icons: List<Int>
) : RecyclerView.Adapter<IconPagerAdapter.Pager2ViewHolder>() {

    private lateinit var mListener: OnCheckIconClickListener

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val iconTitle: TextView = itemView.findViewById(R.id.title_icon)
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val checkBoxIcon: CheckBox = itemView.findViewById(R.id.checkbox_icon)

        init {
            checkBoxIcon.setOnClickListener(this)
            checkBoxIcon.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!isChecked) {
                    buttonView.isChecked = true
                }
            }
        }

        override fun onClick(itemView: View) {
            val position = adapterPosition
            mListener.onCheckIconClick(itemView, position)
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IconPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false)
        )
    }

    override fun onBindViewHolder(holder: IconPagerAdapter.Pager2ViewHolder, position: Int) {
        onViewRecycled(holder)
        holder.checkBoxIcon.isChecked = false
        holder.checkBoxIcon.isChecked = checkedBox[position]
        holder.iconTitle.setText(titles[position])
        holder.icon.setBackgroundResource(icons[position])
    }

    override fun getItemCount(): Int {
        return checkedBox.size
    }

    fun setOnCardClickListener(listener: OnCheckIconClickListener) {
        mListener = listener
    }

    interface OnCheckIconClickListener {
        fun onCheckIconClick(view: View, position: Int)
    }
}