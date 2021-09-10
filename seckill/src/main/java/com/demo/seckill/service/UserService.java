package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);

    UserModel register(UserModel userModel) throws BusinessException;

    UserModel login(String telphone, String password) throws BusinessException;

    UserModel validateLogin(String telphone, String password) throws BusinessException;
}
