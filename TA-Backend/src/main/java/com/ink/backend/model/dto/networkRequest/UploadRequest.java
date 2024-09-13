package com.ink.backend.model.dto.networkRequest;

import lombok.Data;

@Data
public class UploadRequest {
    private String id;
    private String img;
    private String wav;
}
