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
import com.example.english_personal_training.R
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word_list, parent, false)
        return WordListViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordListViewHolder, position: Int) {
        val word = words[position]
        holder.wordTextView.text = word.word
        holder.meanTextView.text = word.meaning
        holder.progressBar.visibility = View.GONE

        if (examplesMap.containsKey(word.word)) {
            holder.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
            holder.examplesTextView.visibility = View.VISIBLE
            holder.showExamplesButton.text = "예문 접기"
        } else {
            holder.examplesTextView.visibility = View.GONE
            holder.showExamplesButton.text = "예문 보기"
        }

        holder.showExamplesButton.setOnClickListener {
            if (loadingMap[word.word] == true) return@setOnClickListener

            val isExpanding = holder.examplesTextView.visibility == View.GONE
            if (isExpanding) {
                if (examplesMap.containsKey(word.word)) {
                    holder.examplesTextView.text = HtmlCompat.fromHtml(examplesMap[word.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    holder.examplesTextView.visibility = View.VISIBLE
                    holder.showExamplesButton.text = "예문 접기"
                } else {
                    holder.progressBar.visibility = View.VISIBLE
                    holder.examplesTextView.visibility = View.GONE
                    holder.showExamplesButton.text = "예문 로딩 중..."
                    loadExampleAsync(word, holder.examplesTextView, holder.showExamplesButton, holder.progressBar)
                }
            } else {
                holder.examplesTextView.visibility = View.GONE
                holder.showExamplesButton.text = "예문 보기"
            }
        }
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
                showExamplesButton.text = "예문 보기"
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
                        showExamplesButton.text = "예문 접기"
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

    class WordListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordTextView: TextView = itemView.findViewById(R.id.wordTextView)
        val meanTextView: TextView = itemView.findViewById(R.id.MeanTextView)
        val showExamplesButton: Button = itemView.findViewById(R.id.showExamplesButton)
        val examplesTextView: TextView = itemView.findViewById(R.id.examplesTextView)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}
