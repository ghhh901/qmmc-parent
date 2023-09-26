package com.ghhh.qmmc.exception;

import com.ghhh.qmmc.result.ResultCodeEnum;
import lombok.Data;

@Data
public class QmmcException extends RuntimeException {

    private Integer code;

    public QmmcException(String message,Integer code){
        super(message);
        this.code = code;
    }

    public QmmcException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "QmmcException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
