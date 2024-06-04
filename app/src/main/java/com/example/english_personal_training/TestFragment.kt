import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.english_personal_training.ComposingTestActivity
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemDatabase
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.databinding.FragmentTestBinding
import com.example.englishquiz.WordTestItem
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding
    private val itemViewModel: ItemViewModel by viewModels()
    private var wordList: List<WordTestItem> = listOf()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var totalSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        observeViewModel()

        // ComposingTestActivity를 팝업으로 띄우기
        val intent = Intent(requireContext(), ComposingTestActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_COMPOSING_TEST)

        binding.resultCheckButton.setOnClickListener {
            showResultDialog(totalSelected, correctAnswers)
        }

        return binding.root
    }

    private fun observeViewModel() {
        // DB에서 Observer 통해서 단어 불러오기
        itemViewModel.allItems.observe(viewLifecycleOwner, Observer { items ->
            val allWords = items.map { it.word }
            wordList = items.map { item ->
                WordTestItem(
                    word = item.word,
                    meaning = item.meaning,
                    options = generateOptions(item.word, allWords)
                )
            }.shuffled(Random(System.currentTimeMillis())) // 단어 문제 섞기
            showNextQuestion()
        })
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < wordList.size) {
            val currentQuestion = wordList[currentQuestionIndex]
            binding.questionTextView.text = currentQuestion.meaning

            val options = currentQuestion.options.shuffled()
            binding.optionButton1.text = options[0]
            binding.optionButton2.text = options[1]
            binding.optionButton3.text = options[2]
            binding.optionButton4.text = options[3]

            binding.optionButton1.setOnClickListener { handleOptionClick(options[0], currentQuestion.word) }
            binding.optionButton2.setOnClickListener { handleOptionClick(options[1], currentQuestion.word) }
            binding.optionButton3.setOnClickListener { handleOptionClick(options[2], currentQuestion.word) }
            binding.optionButton4.setOnClickListener { handleOptionClick(options[3], currentQuestion.word) }
        } else {
            showResultDialog(totalSelected, correctAnswers)
        }
    }

    private fun handleOptionClick(selectedOption: String, correctAnswer: String) {
        totalSelected++
        if (selectedOption == correctAnswer) correctAnswers++

        val toastMessage = if (selectedOption == correctAnswer) { "정답입니다!" } else { "오답입니다!" }
        val toast = Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
        toast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
            currentQuestionIndex++
            showNextQuestion()
        }, 800)
    }

    private fun generateOptions(correctWord: String, allWords: List<String>): List<String> {
        val shuffled = allWords.filter { it != correctWord }.shuffled()
        return (shuffled.take(3) + correctWord).shuffled()
    }

    private fun showResultDialog(totalSelected: Int, correctAnswers: Int) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("결과 확인")
            .setMessage("총 선택한 문제: $totalSelected\n맞힌 문제: $correctAnswers\n틀린 문제: ${totalSelected - correctAnswers}")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_COMPOSING_TEST && resultCode == AppCompatActivity.RESULT_OK) {
            val problemCount = data?.getIntExtra("PROBLEM_COUNT", 0) ?: 0
            val selectedType = data?.getStringExtra("SELECTED_TYPE") ?: ""
            val selectedSet = data?.getStringExtra("SELECTED_SET") ?: ""

            // 가져온 데이터로 테스트 화면 초기화
            lifecycleScope.launch {
                val words = getWordsFromDatabase(selectedSet)
                wordList = words.map { WordTestItem(it.word, it.meaning, generateOptions(it.word, words.map { it.word })) }
                currentQuestionIndex = 0
                correctAnswers = 0
                totalSelected = 0
                showNextQuestion()
            }
        }
    }

    private suspend fun getWordsFromDatabase(tag: String): List<Item> {
        return withContext(Dispatchers.IO) {
            val db = ItemDatabase.getDatabase(requireContext())
            db.itemDao().getAllItems().filter { it.tag == tag }
        }
    }

    companion object {
        const val REQUEST_CODE_COMPOSING_TEST = 1
    }
}
