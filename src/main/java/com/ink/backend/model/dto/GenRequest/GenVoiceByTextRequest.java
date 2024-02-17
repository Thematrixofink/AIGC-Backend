package com.ink.backend.model.dto.GenRequest;


import lombok.Data;

/**
 * 通过文字生成音频请求类
 */
@Data
public class GenVoiceByTextRequest {
    /**
     * 用户输入
     */
    private String userInput;
}
