package com.example.docent

data class ChatMessage(
    val text: String,       // 메시지 내용
    val isUser: Boolean     // true이면 사용자, false이면 챗봇
)