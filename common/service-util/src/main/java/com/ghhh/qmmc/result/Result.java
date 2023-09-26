package com.ghhh.qmmc.result;

import lombok.Data;

@Data
public class Result<T> {
    //状态码
    private Integer code;
    //消息
    private String message;
    //数据
    private T data;

    //构造器私有化
    private Result(){}

    public static<T> Result<T> build(T data,Integer code,String message){
        Result<T> result = new Result<>();
        if (data != null){
            result.setData(data);
        }
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static<T> Result<T> build(T data,ResultCodeEnum resultCodeEnum){
        Result<T> result = new Result<>();
        if (data != null){
            result.setData(data);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static<T> Result<T> ok(T data){
        return build(data,ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> fail(T data){
        return build(data,ResultCodeEnum.FAIL);
    }
}
