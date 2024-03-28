package com.aixbox.usercenter.exception;

import com.aixbox.usercenter.common.BaseResponse;
import com.aixbox.usercenter.common.ErrorCode;
import com.aixbox.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author 魔王Aixbox
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("businessException：" + e.getMessage(), e.getDescription());
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }


}
