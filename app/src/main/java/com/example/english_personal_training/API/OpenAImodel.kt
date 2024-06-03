package com.example.englishquiz

// OpenAIRequest 데이터 클래스는 OpenAI API 요청의 본문을 나타냅니다.
data class OpenAIRequest(
    val model: String, // 사용할 GPT 모델의 이름
    val messages: List<Message> // 메시지 목록
)

// Message 데이터 클래스는 OpenAI API 요청과 응답에서 사용되는 메시지를 나타냅니다.
data class Message(
    val role: String, // 메시지의 역할 (예: "system", "user", "assistant")
    val content: String // 메시지의 내용
)

// OpenAIResponse 데이터 클래스는 OpenAI API 응답의 본문을 나타냅니다.
data class OpenAIResponse(
    val choices: List<Choice> // 응답에서 선택된 메시지 목록
)

// Choice 데이터 클래스는 OpenAI API 응답에서 선택된 메시지를 나타냅니다.
data class Choice(
    val message: Message // 선택된 메시지의 내용
)
