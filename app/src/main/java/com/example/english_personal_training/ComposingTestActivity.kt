package com.example.english_personal_training

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.english_personal_training.data.ItemDatabase
import com.example.english_personal_training.databinding.ActivityComposingTestBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComposingTestActivity : AppCompatActivity() {

    private var selectedType: String = ""
    private var selectedSet: String = ""
    private var problemCount: Int = 0
    private lateinit var binding: ActivityComposingTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 팝업창 크기 설정
        val window = window
        val layoutParams = window.attributes
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 너비를 화면 너비의 90%로 설정
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt() // 높이를 화면 높이의 80%로 설정
        window.attributes = layoutParams

        // 테스트 유형
        binding.testType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.test_type_def -> selectedType = "뜻"
                R.id.test_type_ex -> selectedType = "예문"
                else -> selectedType = ""
            }
        }

        // 기본 항목을 포함한 어댑터 설정
        val defaultTags = listOf("No tags available")
        val adapter = ArrayAdapter(this@ComposingTestActivity, android.R.layout.simple_spinner_item, defaultTags)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.testSpinner.adapter = adapter

        // 데이터베이스에서 태그 목록을 가져와서 스피너에 설정
        lifecycleScope.launch {
            val tags = getTagsFromDatabase()
            withContext(Dispatchers.Main) {
                if (tags.isNotEmpty()) {
                    val newAdapter = ArrayAdapter(this@ComposingTestActivity, android.R.layout.simple_spinner_item, tags)
                    newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.testSpinner.adapter = newAdapter
                } else {
                    Log.w("ComposingTestActivity", "No tags found in the database")
                }
            }
        }

        // 스피너에서 태그 선택하기
        binding.testSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedSet = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSet = ""
            }
        }

        // 테스트 시작 버튼
        binding.testStart.setOnClickListener {
            problemCount = binding.testProb.text.toString().toIntOrNull() ?: 0

            var validInput = true
            val missingInputs = mutableListOf<String>()

            if (problemCount <= 0) {
                missingInputs.add("문제 수")
                validInput = false
            }

            if (selectedType.isEmpty()) {
                missingInputs.add("테스트 유형")
                validInput = false
            }

            if (selectedSet.isEmpty() || selectedSet == "No tags available") {
                missingInputs.add("태그")
                validInput = false
            }

            if (validInput) {
                val resultIntent = Intent().apply {
                    putExtra("PROBLEM_COUNT", problemCount)
                    putExtra("SELECTED_TYPE", selectedType)
                    putExtra("SELECTED_SET", selectedSet)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                if (missingInputs.size > 1) {
                    Toast.makeText(this, "모두 선택하고 테스트 시작 버튼을 눌러주세요", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "${missingInputs.joinToString(", ")}를 선택하세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getTagsFromDatabase(): List<String> {
        return withContext(Dispatchers.IO) {
            val db = ItemDatabase.getDatabase(applicationContext)
            val tags = db.itemDao().getAllItems().map { it.tag }.distinct()
            tags
        }
    }
}
