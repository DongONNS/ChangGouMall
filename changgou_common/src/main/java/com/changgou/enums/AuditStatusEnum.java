package com.changgou.enums;

/**
 * 定义商品的审核状态
 */
public enum AuditStatusEnum {
    WAITING_FOR_AUDIT("0","未审核"),
    AUDIT_PASSED("1","审核通过"),
    AUDIT_NOT_PASSED("2","审核未通过");

    private String code;
    private String msg;

    AuditStatusEnum(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
