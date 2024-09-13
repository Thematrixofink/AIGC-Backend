package com.ink.backend.model.dto.GenRequest;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenRequest implements Serializable {
    /**
     * 用户的输入
     */
    private String userInput;

    /**
     * 对话的Id
     */
    private String messageId;

//    /**
//     * 是否需要生成视频
//     */
//    private boolean needVideo;


    private static final long serialVersionUID = 1L;
}
