package com.example.englishquiz

//data class WordItem(val word: String, val meaning: String, val options: List<String>)
data class WordItem(
    val word: String,
    val meaning: String,
    val options: List<String>,
    var userChoice: String? = null // 사용자의 선택을 저장하는 필드 추가
)

