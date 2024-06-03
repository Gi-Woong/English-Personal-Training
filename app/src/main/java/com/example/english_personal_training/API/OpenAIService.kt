package com.example.englishquiz

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// OpenAIService 인터페이스는 OpenAI API와 통신하는 Retrofit 인터페이스입니다.
interface OpenAIService {

    // 요청 헤더에 Content-Type을 application/json으로 설정
    @Headers("Content-Type: application/json")

    // POST 메서드를 사용하여 /v1/chat/completions 엔드포인트에 요청을 보냅니다.
    @POST("v1/chat/completions")

    // generateExample 메서드는 OpenAIRequest 객체를 본문으로 받아 OpenAIResponse 객체를 반환하는 Retrofit Call을 생성합니다.
    fun generateExample(@Body request: OpenAIRequest): Call<OpenAIResponse>
}
