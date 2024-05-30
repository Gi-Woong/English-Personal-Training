package com.example.englishquiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    lateinit var binding: FragmentTestBinding
    private lateinit var wordAdapter: WordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        initRecyclerView()

        binding.resultCheckButton.setOnClickListener {
            var totalSelected = 0
            var correctAnswers = 0
            wordAdapter.wordList.forEach {
                if (it.userChoice != null) { // 선택된 옵션이 있는 경우
                    totalSelected++
                    if (it.word == it.userChoice) correctAnswers++ // 정답일 경우
                }
            }
            Toast.makeText(context, "선택한 답: $totalSelected, 정답 수: $correctAnswers", Toast.LENGTH_SHORT).show()
        }

        return binding.root

    }

    private fun initRecyclerView() {
        val allWords = listOf("apple", "star", "cord", "key", "house", "go", "peach")

        val wordList = listOf(
            WordItem("apple", "사과", generateOptions("apple", allWords)),
            WordItem("star", "별", generateOptions("star", allWords)),
            WordItem("cord", "코드", generateOptions("cord", allWords)),
            WordItem("key", "열쇠", generateOptions("key", allWords)),
            WordItem("house", "집", generateOptions("house", allWords)),
            WordItem("go", "가다", generateOptions("go", allWords))
        )

        wordAdapter = WordAdapter(wordList) { word, option ->
            if (word == option) {
                Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "오답입니다!", Toast.LENGTH_SHORT).show()
            }

        }

        binding.recyclerView.adapter = wordAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun generateOptions(correctWord: String, allWords: List<String>): List<String> {
        val shuffled = allWords.filter { it != correctWord }.shuffled()
        return (shuffled.take(3) + correctWord).shuffled()
    }

}

