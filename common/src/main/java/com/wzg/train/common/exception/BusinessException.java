package com.wzg.train.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class BusinessException extends RuntimeException{


    private BusinessExceptionEnum e;


    //不写入堆栈，提升性能
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
