package com.changgou.entity;

import lombok.Data;

import java.io.ObjectInput;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private boolean flag;//是否成功
    private Integer code;//返回码
    private String message;//返回消息
    private T data;//返回数据

    public Result(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

    public Result (boolean flag,Integer code,String message){
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public Result(String message, Object data){
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = message;
        this.data = (T)data;
    }



    public Result(String message) {
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = message;
    }

    public Result() {
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = "操作成功!";
    }

}
