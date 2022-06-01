package com.forceless.actionclock

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.forceless.actionclock.databinding.FragmentItemBinding

class MyClassItemRecyclerViewAdapter(
    private val values: Array<String>
) : RecyclerView.Adapter<MyClassItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = position.toString()
        holder.textView.text = item
    }
    override fun getItemCount(): Int = values.size
    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val textView = binding.itemClassText
    }

}