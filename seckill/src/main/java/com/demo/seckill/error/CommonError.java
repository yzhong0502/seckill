package com.demo.seckill.error;

public interface CommonError {
    public String getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
