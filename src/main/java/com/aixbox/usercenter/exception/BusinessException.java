package com.aixbox.usercenter.exception;

import com.aixbox.usercenter.common.ErrorCode;

/**
 * 业务异常类
 *
 * @author 魔王Aixbox
 * @version 1.0
 */
public class BusinessException extends RuntimeException{
    private int code;
    private String description;

    public BusinessException(String message ,int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description){
        super(description);
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
