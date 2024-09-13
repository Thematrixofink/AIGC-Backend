package com.ink.backend.model.dto.networkRequest;

import lombok.Data;

import java.util.List;

@Data
public class ERNIEBotRequest {

    /**
     * 对话信息
     */
    private List<String> messages;
}
