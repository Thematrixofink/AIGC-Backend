package com.ink.backend.model.dto.GenRequest;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class StatusResponse {
    /**
     * 状态
     */
    private String status;

    /**
     * 失败信息
     */
    private String execMessage;

    private String task;
}
