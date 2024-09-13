package com.ink.backend.common;

import com.ink.backend.exception.BusinessException;

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

    public static String getMessageByCode(int code){
        if(code == TO_GEN.getCode()){
            return TO_GEN.message;
        }else if (code == GENING.getCode()){
            return GENING.message;
        }else if(code == SUCCESS.getCode()){
            return SUCCESS.getMessage();
        }else if (code == FAIL.getCode()){
            return FAIL.getMessage();
        }else{
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"此任务状态码不存在");
        }
    }
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
