package com.ink.backend.model.dto.bottle;

import lombok.Data;

import java.io.Serializable;

@Data
public class BottleAddRequest implements Serializable {
    /**
     * 漂流瓶内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
