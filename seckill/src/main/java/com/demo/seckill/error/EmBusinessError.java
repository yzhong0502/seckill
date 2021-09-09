package com.demo.seckill.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型10000
    PARAMETER_VALIDATION_ERROR(10001, "PARAMETER NOT VALID"),
    UNKNOWN_ERROR(10002, "UNKNOWN ERROR"),

    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "USER NOT EXIST"),
    USER_LOGIN_FAIL(20002, "PASSWORD INVALID"),

    //30000开头为item相关错误定义
    ITEM_STOCK_NOT_ENOUGH(30001, "Item stock not enough"),
    ORDER_NOT_FOUND(30002,"Order not found"),
    ITEM_NOT_FOUND(30003, "Item not found")

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
