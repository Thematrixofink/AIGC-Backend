package com.ink.backend.model.dto.networkRequest;

import lombok.Data;

@Data
public class MessageModelRequest {
    private String role;

    private String content;
}
