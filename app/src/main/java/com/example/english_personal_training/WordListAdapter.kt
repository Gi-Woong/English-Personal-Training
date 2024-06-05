package com.example.englishquiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.english_personal_training.BuildConfig
import com.example.english_personal_training.databinding.ItemWordListBinding
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WordListAdapter : RecyclerView.Adapter<WordListAdapter.WordListViewHolder>() {

    private var words: List<WordTestItem> = emptyList()
    private val examplesMap = mutableMapOf<String, String>()
    private val loadingMap = mutableMapOf<String, Boolean>()
    private val apiKey = BuildConfig.API_KEY
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
        if (!word.word.matches(Regex("^[a-zA-Z ]+$")) || !word.meaning.matches(Regex("^[가-힣 ]+$"))) {
            CoroutineScope(Dispatchers.Main).launch {
                examplesTextView.text = "Failed to load example."
                examplesTextView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                toggleShowExamplesButtonText(showExamplesButton, true)
            }
            return
        }

        loadingMap[word.word] = true

        CoroutineScope(Dispatchers.IO).launch {
            val request = OpenAIRequest(
                model = "gpt-4-turbo",
                messages = listOf(
                    Message(role = "system", content = "You are a helpful assistant."),
                    Message(role = "user", content = """
                        Provide an example sentence for the word '${word.word}' in English and its Korean translation. 
                        The sentence should clearly demonstrate the meaning '${word.meaning}' of the word '${word.word}'. 
                        Make sure the sentence is clear and grammatically correct. 
                        Return the result in the format: 
                        'English: <example sentence><newline>Korean: <translation>'.
                        Make sure the Korean translation is natural and grammatically correct. 
                        Bold the word '${word.word}' in the English sentence using HTML <b> tags.
                    """.trimIndent())
                )
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
                        progressBar.visibility = View.GONE
                        toggleShowExamplesButtonText(showExamplesButton, true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        examplesTextView.text = "Failed to load example."
                        examplesTextView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        toggleShowExamplesButtonText(showExamplesButton, true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    examplesTextView.text = "Error: ${e.message}"
                    examplesTextView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    toggleShowExamplesButtonText(showExamplesButton, true)
                }
            } finally {
                loadingMap[word.word] = false
            }
        }
    }

    private fun toggleShowExamplesButtonText(showExamplesButton: Button, isExpanding: Boolean) {
        showExamplesButton.text = if (isExpanding) "예문 접기" else "예문 보기"
    }

    inner class WordListViewHolder(private val binding: ItemWordListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: WordTestItem) {
            binding.wordTextView.text = word.word
            binding.meanTextView.text = word.meaning
            binding.progressBar.visibility = View.GONE

            if (examplesMap.containsKey(word.word)) {
                binding.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.examplesTextView.visibility = View.VISIBLE
                toggleShowExamplesButtonText(binding.showExamplesButton, true)
            } else {
                binding.examplesTextView.visibility = View.GONE
                toggleShowExamplesButtonText(binding.showExamplesButton, false)
            }

            binding.showExamplesButton.setOnClickListener {
                if (loadingMap[word.word] == true) return@setOnClickListener

                val isExpanding = binding.examplesTextView.visibility == View.GONE
                if (isExpanding) {
                    if (examplesMap.containsKey(word.word)) {
                        binding.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        binding.examplesTextView.visibility = View.VISIBLE
                        toggleShowExamplesButtonText(binding.showExamplesButton, true)
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.examplesTextView.visibility = View.GONE
                        binding.showExamplesButton.text = "예문 로딩 중..."
                        loadExampleAsync(word, binding.examplesTextView, binding.showExamplesButton, binding.progressBar)
                    }
                } else {
                    binding.examplesTextView.visibility = View.GONE
                    toggleShowExamplesButtonText(binding.showExamplesButton, false)
                }
            }
        }
    }
}
