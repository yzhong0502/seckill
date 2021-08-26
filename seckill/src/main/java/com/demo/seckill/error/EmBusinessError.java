package com.demo.seckill.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型00001
    PARAMETER_VALIDATION_ERROR(10001, "PARAMETER NOT VALID"),
    UNKNOWN_ERROR(10002, "UNKNOWN ERROR"),

    //10000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "USER NOT EXIST")
    ;

    private int errCode;
    private String errMsg;

    private EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
