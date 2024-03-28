package com.aixbox.usercenter.common;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private  T data;
    private String message;
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data){
        this(code,data,"", "");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null, errorCode.getMessage(), errorCode.getDescription());
    }
}