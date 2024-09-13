package com.ink.backend.model.dto.aiChat;

import lombok.Data;

@Data
public class AIChatResponse {
    /**
     * 对话的id
     */
    private Long chatId;

    /**
     * AI的响应
     */
    private String reply;
}
