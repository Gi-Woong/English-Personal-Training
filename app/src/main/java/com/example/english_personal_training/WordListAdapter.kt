package com.example.englishquiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.text.HtmlCompat
import com.example.english_personal_training.BuildConfig
import com.example.english_personal_training.databinding.ItemWordListBinding
import kotlinx.coroutines.*
import okhttp3.Dispatcher

class WordListAdapter : RecyclerView.Adapter<WordListAdapter.WordListViewHolder>() {

    private var words: List<WordTestItem> = emptyList()
    private val examplesMap = mutableMapOf<String, String>()
    private val loadingMap = mutableMapOf<String, Boolean>()
    private val apiKey= BuildConfig.API_KEY
    private val openAIService: OpenAIService

    init {

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }.dispatcher(Dispatcher().apply { maxRequests = 64; maxRequestsPerHost = 5 }).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openAIService = retrofit.create(OpenAIService::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordListViewHolder {
        val binding = ItemWordListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordListViewHolder, position: Int) {
        val word = words[position]
        holder.bind(word)
    }

    override fun getItemCount(): Int = words.size

    fun setWords(words: List<WordTestItem>) {
        this.words = words
        notifyDataSetChanged()
    }

    private fun loadExampleAsync(word: WordTestItem, examplesTextView: TextView, showExamplesButton: Button, progressBar: ProgressBar) {
        if (loadingMap[word.word] == true) return

        // Check if the word and meaning are valid (simple validation)
        if (!word.word.matches(Regex("^[a-zA-ZÀ-ÿ\\s,-]+$")) || !word.meaning.matches(Regex("^[가-힣,~;\\s]+\$"))) {
            CoroutineScope(Dispatchers.Main).launch {
                examplesTextView.text = "Failed to load example."
                examplesTextView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                setButtonText(showExamplesButton, "예문 접기")
            }
            return
        }

        loadingMap[word.word] = true

        CoroutineScope(Dispatchers.IO).launch {
            val request = OpenAIRequest(
                model = "gpt-4-turbo",
                messages = listOf(
                    Message(role = "system", content = "You are a helpful, fastest answering English teacher teaching Korean students in English."),
                    Message(role = "user", content =
                    """
${word.word}
task: Write a appropriate english example sentence.
conditions: 
<a example sentence in korean> contain the literal korean meaning of  provided word
memorizable
easy to speak
Make sure the sentence is clear and grammatically correct. 
You should refer to the official English dictionary.
Find a sentence with as few words as possible
The answer must include the 'literal meaning' in korean.
Bold the word '${word.word}' in the English sentence using HTML <b> tags.
한글 문법을 철저히 지키세요!
[important]
Suggest EASY sentence.
Answer should be two lines.
<a example sentence in korean> should be Natural sentences in Korean
[answer form]
English: <example sentence><newline>Korean: <a example sentence in korean>.
                    """.trimIndent())
                ),
            )

            try {
                val response = openAIService.generateExample(request).execute()
                if (response.isSuccessful) {
                    val exampleText = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                    val formattedText = exampleText.replace("English: ", "")
                        .replace("Korean: ", "")
                        .replace("**", "")
                        .replace(word.word, "<b>${word.word}</b>")
                        .replace("\n", "<br/>")
                    examplesMap[word.word] = formattedText

                    withContext(Dispatchers.Main) {
                        examplesTextView.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        examplesTextView.visibility = View.VISIBLE
                        setButtonText(showExamplesButton, "예문 접기")
                        progressBar.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        examplesTextView.text = "Failed to load example."
                        examplesTextView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    examplesTextView.text = "Error: ${e.message}"
                    examplesTextView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            } finally {
                loadingMap[word.word] = false
            }
        }
    }

    private fun setButtonText(button: Button, text: String) {
        button.text = text
    }

    inner class WordListViewHolder(private val binding: ItemWordListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: WordTestItem) {
            binding.wordTextView.text = word.word
            binding.MeanTextView.text = word.meaning
            binding.progressBar.visibility = View.GONE

            if (examplesMap.containsKey(word.word)) {
                binding.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.examplesTextView.visibility = View.VISIBLE
                setButtonText(binding.showExamplesButton, "예문 접기")
            } else {
                binding.examplesTextView.visibility = View.GONE
                setButtonText(binding.showExamplesButton, "예문 보기")
            }

            binding.showExamplesButton.setOnClickListener {
                if (loadingMap[word.word] == true) return@setOnClickListener

                val isExpanding = binding.examplesTextView.visibility == View.GONE
                if (isExpanding) {
                    if (examplesMap.containsKey(word.word)) {
                        binding.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        binding.examplesTextView.visibility = View.VISIBLE
                        setButtonText(binding.showExamplesButton, "예문 접기")
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.examplesTextView.visibility = View.GONE
                        setButtonText(binding.showExamplesButton, "예문 로딩 중...")
                        loadExampleAsync(word, binding.examplesTextView, binding.showExamplesButton, binding.progressBar)
                    }
                } else {
                    binding.examplesTextView.visibility = View.GONE
                    setButtonText(binding.showExamplesButton, "예문 보기")
                }
            }
        }
    }
}
