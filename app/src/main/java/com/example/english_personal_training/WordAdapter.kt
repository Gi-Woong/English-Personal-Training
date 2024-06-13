import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.english_personal_training.BuildConfig
import com.example.english_personal_training.R
import com.example.english_personal_training.databinding.ItemWordTestBinding
import com.example.englishquiz.Message
import com.example.englishquiz.OpenAIRequest
import com.example.englishquiz.OpenAIService
import com.example.englishquiz.WordTestItem
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WordAdapter(
    private val wordList: List<WordTestItem>,
    private val selectedType: String,
    private val onOptionClicked: (String, String) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private val openAIService: OpenAIService

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .dispatcher(Dispatcher().apply {
                maxRequests = 64
                maxRequestsPerHost = 5
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openAIService = retrofit.create(OpenAIService::class.java)
    }

    private val examplesMap = mutableMapOf<String, String>()
    private val loadingMap = mutableMapOf<String, Boolean>()

    class WordViewHolder(val binding: ItemWordTestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentItem = wordList[position]
        holder.binding.numberText.text = "Q${position + 1}>" // 각 아이템의 번호를 설정

        if (selectedType == "예문") {
            holder.binding.meaningText.text = "예문 로딩 중..."
            holder.binding.meaningText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            holder.binding.meaningText.gravity = View.TEXT_ALIGNMENT_CENTER // 텍스트를 가운데 정렬
            holder.binding.meaningText.setPadding(16, 16, 16, 16) // 패딩 설정
            // 마진 설정
            val layoutParams = holder.binding.meaningText.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(40, 16, 16, 16) // left, top, right, bottom 마진 설정
            holder.binding.meaningText.layoutParams = layoutParams

            if (examplesMap.containsKey(currentItem.word)) {
                holder.binding.meaningText.text = HtmlCompat.fromHtml(examplesMap[currentItem.word] ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                loadExampleAsync(currentItem.word, holder.binding.meaningText)
            }
        } else {
            holder.binding.meaningText.text = currentItem.meaning
            holder.binding.meaningText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            holder.binding.meaningText.gravity = View.TEXT_ALIGNMENT_CENTER // 텍스트를 가운데 정렬
            holder.binding.meaningText.setPadding(16, 16, 16, 16) // 패딩 설정
            // 마진 설정
            val layoutParams = holder.binding.meaningText.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(40, 16, 16, 16) // left, top, right, bottom 마진 설정
            holder.binding.meaningText.layoutParams = layoutParams
        }

        val buttons = listOf(
            holder.binding.optionButton1,
            holder.binding.optionButton2,
            holder.binding.optionButton3,
            holder.binding.optionButton4
        )

        // 모든 버튼을 초기 상태로 설정
        val darkGray = Color.rgb(67, 67, 67)
        buttons.forEach {
            it.setBackgroundColor(darkGray) // 초기 배경색을 어두운 회색으로 설정
        }

        currentItem.options.forEachIndexed { index, option ->
            buttons[index].text = option
            buttons[index].setOnClickListener {
                if (currentItem.userChoice == option) {
                    it.setBackgroundColor(darkGray) // 선택 해제
                    currentItem.userChoice = null
                } else {
                    buttons.forEach { btn -> btn.setBackgroundColor(darkGray) } // 모든 버튼 초기화
                    it.setBackgroundColor(ContextCompat.getColor(it.context, R.color.mint)) // 선택 시 배경색 변경
                    currentItem.userChoice = option
                }
                onOptionClicked(currentItem.word, option)
            }
        }
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

    private fun loadExampleAsync(word: String, meaningText: TextView) {
        if (loadingMap[word] == true) return

        loadingMap[word] = true

        CoroutineScope(Dispatchers.IO).launch {
            val request = OpenAIRequest(
                model = "gpt-4-turbo",
                messages = listOf(
                    Message(role = "system", content = "You are a helpful assistant."),
                    Message(role = "user", content = """
                        Provide an example sentence for the word '${word}' in English. 
                        The sentence should clearly demonstrate the meaning of the word '${word}'. 
                        Make sure the sentence is clear and grammatically correct. 
                        You must include the ${word} in example sentence(double check: this is most important).
                        Bold the word '${word}' in the English sentence using HTML <b> tags.
                    """.trimIndent())
                )
            )

            try {
                val response = openAIService.generateExample(request).execute()
                if (response.isSuccessful) {
                    var exampleText = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                    exampleText = exampleText.substringAfter(":").trim()
                    val wordRegex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
                    val formattedText = exampleText
                        .replace("**", "")
                        .replace(wordRegex, "______")
                    examplesMap[word] = formattedText

                    withContext(Dispatchers.Main) {
                        meaningText.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        meaningText.text = "Failed to load example."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    meaningText.text = "Error: ${e.message}"
                }
            } finally {
                loadingMap[word] = false
            }
        }
    }
}
