package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;

    void login(String telphone, String password);
}
