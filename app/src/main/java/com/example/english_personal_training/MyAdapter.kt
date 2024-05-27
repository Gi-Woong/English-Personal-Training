package com.example.english_personal_training

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.english_personal_training.databinding.ItemLayoutBinding

class MyAdapter(private val itemList: MutableList<Item>) : RecyclerView.Adapter<MyAdapter.ItemViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return itemList[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
        holder.itemView.setOnClickListener {
            holder.toggleEditMode()
        }
        holder.binding.doneButton.setOnClickListener {
            holder.toggleEditMode()
            // 수정된 내용을 TextView에 반영
            holder.tagTextView.text = holder.tagEditTextView.text
            holder.wordTextView.text = holder.wordEditTextView.text
            holder.meaningTextView.text = holder.meaningEditTextView.text
        }
        holder.binding.deleteButton.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class ItemViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val tagTextView = binding.tagTextView
        val wordTextView = binding.wordTextView
        val meaningTextView = binding.meaningTextView
        val tagEditTextView = binding.tagEditTextView
        val wordEditTextView = binding.wordEditTextView
        val meaningEditTextView = binding.meaningEditTextView
        val doneButton = binding.doneButton

        fun bind(item: Item) {
            // TextView에 텍스트 설정
            tagTextView.text = item.tag
            wordTextView.text = item.word
            meaningTextView.text = item.meaning

            // EditText에 TextView의 텍스트 설정
            tagEditTextView.setText(item.tag)
            wordEditTextView.setText(item.word)
            meaningEditTextView.setText(item.meaning)

            // 초기에는 EditText를 보이지 않도록 설정
            tagEditTextView.visibility = android.view.View.GONE
            wordEditTextView.visibility = android.view.View.GONE
            meaningEditTextView.visibility = android.view.View.GONE
            doneButton.visibility = android.view.View.GONE
        }

        fun toggleEditMode() {
            // 수정 모드 토글
            val isEditMode = tagTextView.visibility == android.view.View.GONE
            tagTextView.visibility = if (isEditMode) android.view.View.VISIBLE else android.view.View.GONE
            wordTextView.visibility = if (isEditMode) android.view.View.VISIBLE else android.view.View.GONE
            meaningTextView.visibility = if (isEditMode) android.view.View.VISIBLE else android.view.View.GONE
            tagEditTextView.visibility = if (isEditMode) android.view.View.GONE else android.view.View.VISIBLE
            wordEditTextView.visibility = if (isEditMode) android.view.View.GONE else android.view.View.VISIBLE
            meaningEditTextView.visibility = if (isEditMode) android.view.View.GONE else android.view.View.VISIBLE
            doneButton.visibility = if (isEditMode) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    private fun removeItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemList.size)
    }
}
