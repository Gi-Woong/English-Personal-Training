package com.example.englishquiz

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishquiz.databinding.ItemWordBinding


class WordAdapter(
    val wordList: List<WordItem>,
    private val onOptionClicked: (String, String) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentItem = wordList[position]
        holder.binding.meaningText.text = currentItem.meaning

        val buttons = listOf(
            holder.binding.optionButton1,
            holder.binding.optionButton2,
            holder.binding.optionButton3,
            holder.binding.optionButton4
        )

        // 모든 버튼을 초기 상태로 설정
        buttons.forEach {
            it.setBackgroundColor(Color.GRAY) // 초기 배경색으로 설정
        }

        currentItem.options.forEachIndexed { index, option ->
            buttons[index].text = option
            buttons[index].setOnClickListener {
                currentItem.userChoice = if (currentItem.userChoice == option) {
                    it.setBackgroundColor(Color.GRAY) // 선택 해제
                    null
                } else {
                    buttons.forEach { btn -> btn.setBackgroundColor(Color.GRAY) } // 모든 버튼 초기화
                    it.setBackgroundColor(Color.GREEN) // 선택 시 배경색 변경
                    option
                }
                onOptionClicked(currentItem.word, option)
            }
        }


    }

    override fun getItemCount(): Int {
        return wordList.size
    }
}
