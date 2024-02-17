package com.ink.backend.model.dto.GenRequest;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建数字人的请求
 */
@Data
public class GenAIRequest implements Serializable {
    /**
     * 对AI的称谓
     */
    private String aiName;

    /**
     * 对AI数字人的简介
     */
    private String aiProfile;

    /**
     * 音频（视频）文件地址
     */
    private String aiVoice;

    /**
     * 图片文件地址
     */
    private String aiPicture;

    private static final long serialVersionUID = 1L;
}
