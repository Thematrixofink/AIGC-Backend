package com.ink.backend.model.vo;

import lombok.Data;

@Data
public class GenVideoResponse {
    /**
     * 响应的文字
     */
    private String aiMessage;

//    /**
//     * 相应的视频的连接
//     */
//    private String aiVoiceUrl;

    /**
     * 相应的视频的连接
     */
    private String aiVideoUrl;
}
