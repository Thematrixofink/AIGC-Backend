package com.ink.backend.model.dto.aiChat;

import lombok.Data;

@Data
public class AIChatRequest {
    /**
     * 对话的id
     */
    private Long chatId;

    /**
     * 用户的输入
     */
    private String userInput;
}
