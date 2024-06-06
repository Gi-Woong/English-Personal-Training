package com.example.english_personal_training

import WordAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemDatabase
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.databinding.FragmentTestBinding
import com.example.englishquiz.WordTestItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding
    private val itemViewModel: ItemViewModel by viewModels()
    private var wordList: List<WordTestItem> = listOf()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var totalSelected = 0
    private var selectedType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater, container, false)

        // ComposingTestActivity를 팝업으로 띄우기
        val intent = Intent(requireContext(), ComposingTestActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_COMPOSING_TEST)

        binding.resultCheckButton.setOnClickListener {
            var totalSelected = 0
            var correctAnswers = 0
            val incorrectAnswers = mutableListOf<Int>()

            wordList.forEachIndexed { index, item ->
                if (item.userChoice != null) { // 선택된 옵션이 있는 경우
                    totalSelected++
                    if (item.word == item.userChoice) {
                        correctAnswers++
                    } else {
                        incorrectAnswers.add(index + 1) // 틀린 문제의 번호를 저장
                    }
                }
            }

            if (totalSelected == 0) {
                Toast.makeText(context, "선택된 문제가 하나도 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                showResultDialog(totalSelected, correctAnswers, incorrectAnswers)
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_COMPOSING_TEST && resultCode == AppCompatActivity.RESULT_OK) {
            val problemCount = data?.getIntExtra("PROBLEM_COUNT", 0) ?: 0
            selectedType = data?.getStringExtra("SELECTED_TYPE") ?: ""
            val selectedSet = data?.getStringExtra("SELECTED_SET") ?: ""
            val randomWords = data?.getParcelableArrayListExtra<Item>("RANDOM_WORDS") ?: emptyList()

            // 가져온 데이터로 테스트 화면 초기화
            lifecycleScope.launch {
                val words = getWordsFromDatabase(selectedSet)
                val wordList = words.map {
                    WordTestItem(
                        it.word,
                        it.meaning,
                        generateOptions(it.word, words.map { it.word })
                    )
                }.take(problemCount)
                initRecyclerView(wordList)
            }
        }
    }

    private suspend fun getWordsFromDatabase(tag: String): List<Item> {
        return withContext(Dispatchers.IO) {
            val db = ItemDatabase.getDatabase(requireContext())
            db.itemDao().getAllItems().filter { it.tag == tag }
        }
    }

    private fun generateOptions(correctWord: String, allWords: List<String>): List<String> {
        val shuffled = allWords.filter { it != correctWord }.shuffled()
        return (shuffled.take(3) + correctWord).shuffled()
    }

    private fun initRecyclerView(wordList: List<WordTestItem>) {
        this.wordList = wordList
        val wordAdapter = WordAdapter(wordList, selectedType) { word, option ->
            val toastMessage = if (word == option) { "정답입니다!" } else { "오답입니다!" }
            val toast = Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
            toast.show()

            Handler(Looper.getMainLooper()).postDelayed({
                toast.cancel()
            }, 800)
        }

        binding.recyclerView.adapter = wordAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun showResultDialog(totalSelected: Int, correctAnswers: Int, incorrectAnswers: List<Int>) {
        val incorrectString = if (incorrectAnswers.isNotEmpty()) {
            "틀린 문제 번호: ${incorrectAnswers.joinToString(", ")}"
        } else {
            "모든 문제를 맞혔습니다."
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("결과 확인")
            .setMessage("총 선택한 문제: $totalSelected\n맞힌 문제: $correctAnswers\n틀린 문제: ${totalSelected - correctAnswers}\n$incorrectString")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    companion object {
        const val REQUEST_CODE_COMPOSING_TEST = 1
    }
}
