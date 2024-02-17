package com.ink.backend.model.vo;

import lombok.Data;

/**
 * 生成音频响应类
 */
@Data
public class GenVoiceResponse {
    /**
     * 响应的文字
     */
    private String aiMessage;

    /**
     * 相应的音频的连接
     */
    private String aiVoiceUrl;
}
