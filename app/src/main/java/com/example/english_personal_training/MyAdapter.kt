package com.example.english_personal_training

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.english_personal_training.databinding.ItemLayoutBinding

class MyAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<MyAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    inner class ItemViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.tagTextView.text = item.tag
            binding.wordTextView.text = item.word
            binding.meaningTextView.text = item.meaning
        }
    }
}
