package com.example.englishquiz

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.english_personal_training.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.buttonHowToUse.setOnClickListener {
            showHowToUseDialog()
        }
        return binding.root
    }
    private fun showHowToUseDialog() {
        val message = """
        <b>이 앱은 단어를 GPT를 활용하여 단어에 대한 예문을 살펴보고<br>
        모의 테스트를 통해 암기할 수 있도록 도와주는 앱입니다.</b><br><br>
        
        <h2>WordSet</h2>
        WordSet에서 단어를 추가하거나 삭제 수정해보세요.<br>
        태그(TAG), 단어(WORD), 뜻(MEANING)을 모두 추가해야 등록할 수 있습니다.<br>
        
        <h5>CSV</h5>
        CSV(Comma-Separated Values)파일로도 간편하게 단어를 불러올 수 있습니다.<br>
        (단, CSV의 헤더는 <code>|tag|word|meaning|</code>으로 이루어져 있어야 하며, UTF-8 형식이여야 합니다.<br>
        모든 단어를 삭제해야 할 경우, 전체 삭제 버튼을 눌러보세요.<br>
            
        <h5>태그별 단어 보기</h5>
        "태그별 단어 보기" 버튼을 클릭하면 태그별 단어 묶음과 해당 묶음의 단어 예문을 확인할 수 있습니다.<br>
        '예문보기' 버튼을 눌러 GPT가 제공하는 영어 예문을 확인해보세요. (인칭형 대명사는 작동하지 않을 수 있습니다.)<br><br>
        
        <h2>Test</h2>
        원하는 문제 수와 테스트 형식, 그리고 문제 묶음을 선택하면 단어테스트를 볼 수 있습니다.<br>
        문제를 풀고 결과확인을 통해 맞힌 문제 수, 틀린 문제 수, 틀린 문제 번호를 확인하여 실력을 점검할 수 있습니다.<br>
        예문 유형을 선택할 경우 GPT가 제공하는 예문 빈칸 추론 문제를 풀 수 있습니다!<br><br><br>
        
        EPT와 함께 간편하고 효율적으로 단어를 암기해보세요!
        """.trimIndent()

        val spannedMessage: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(message)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("사용법 안내")
            .setMessage(spannedMessage)
            .setPositiveButton("알겠습니다.") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}