package com.ink.backend.common;

/**
 * 生成状态枚举类
 */
public enum StatusCode {
    TO_GEN(0,"待生成"),
    GENING(1,"生成中"),
    SUCCESS(2, "生成成功"),
    FAIL(3,"生成失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
