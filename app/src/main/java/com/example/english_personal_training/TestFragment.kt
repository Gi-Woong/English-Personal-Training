import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.WordAdapter
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.databinding.FragmentTestBinding
import com.example.englishquiz.WordTestItem

class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding
    private lateinit var wordAdapter: WordAdapter
    private val itemViewModel: ItemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        observeViewModel()

        binding.resultCheckButton.setOnClickListener {
            var totalSelected = 0
            var correctAnswers = 0
            wordAdapter.wordList.forEach {
                if (it.userChoice != null) { // 선택된 옵션이 있는 경우
                    totalSelected++
                    if (it.word == it.userChoice) correctAnswers++ // 정답일 경우
                }
            }
            showResultDialog(totalSelected, correctAnswers)
        }

        return binding.root
    }

    private fun observeViewModel() {
        // DB에서 Observer 통해서 단어 불러오기
        itemViewModel.allItems.observe(viewLifecycleOwner, Observer { items ->
            val allWords = items.map { it.word }
            val wordList = items.map { item ->
                WordTestItem(
                    word = item.word,
                    meaning = item.meaning,
                    options = generateOptions(item.word, allWords)
                )
            }
            initRecyclerView(wordList)
        })
    }

    private fun initRecyclerView(wordList: List<WordTestItem>) {
        wordAdapter = WordAdapter(wordList) { word, option ->
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
}
