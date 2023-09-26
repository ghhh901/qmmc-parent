package com.ghhh.qmmc.exception;

import com.ghhh.qmmc.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//AOP
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail(null);
    }

    @ExceptionHandler(QmmcException.class)
    @ResponseBody
    public Result error(QmmcException e){
        return Result.build(null,e.getCode(),e.getMessage());
    }

}
