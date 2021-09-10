package com.demo.seckill.service.impl;

import com.demo.seckill.entity.UserCreDO;
import com.demo.seckill.entity.UserDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.repository.UserCreDOMapper;
import com.demo.seckill.repository.UserDOMapper;
import com.demo.seckill.service.UserService;
import com.demo.seckill.service.model.UserModel;
import com.demo.seckill.validator.ValidationResult;
import com.demo.seckill.validator.ValidatorImp;
import com.mysql.cj.protocol.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Encoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {

    private UserDOMapper userDOMapper;
    private UserCreDOMapper userCreDOMapper;
    private ValidatorImp validator;

    @Autowired
    public UserServiceImpl(UserDOMapper userDOMapper, UserCreDOMapper userCreDOMapper, ValidatorImp validator) {
        this.userCreDOMapper = userCreDOMapper;
        this.userDOMapper = userDOMapper;
        this.validator = validator;
    }

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) return null;
        UserCreDO userCreDO = userCreDOMapper.selectByUserId(id);
        return convertFromDataObject(userDO, userCreDO);
    }

    @Override
    @Transactional
    public UserModel register(UserModel userModel) throws BusinessException {
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        UserDO userDO = this.convertFromModel(userModel);
        try {
            this.userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Telephone already exists!");
        }

        userModel.setId(userDO.getId());
        if (StringUtils.isEmpty(userModel.getEncryptedPassword()) || userModel.getId() == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        try {
            userModel.setEncryptedPassword(encodeByMD5(userModel.getEncryptedPassword()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        UserCreDO userCreDO = this.convertCreFromModel(userModel);
        this.userCreDOMapper.insertSelective(userCreDO);

        return this.convertFromDataObject(userDO, userCreDO);

    }

    public String encodeByMD5(String str) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String encrypted = base64Encoder.encode(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return encrypted;
    }

    @Override
    public UserModel login(String telphone, String password) throws BusinessException {
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        return validateLogin(telphone, password);
        //login successfull, save token somewhere

    }

    @Override
    public UserModel validateLogin(String telphone, String password) throws BusinessException {
        UserDO userDO = this.userDOMapper.selectByTelphone(telphone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserCreDO userCreDO = this.userCreDOMapper.selectByUserId(userDO.getId());
        try {
            if (!StringUtils.equals(userCreDO.getEncryptPassword(), encodeByMD5(password))) {
                throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return this.convertFromDataObject(userDO, userCreDO);
    }

    private UserModel convertFromDataObject(UserDO userDO, UserCreDO userCreDO) {
        if (userDO == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userCreDO != null) userModel.setEncryptPassword(userCreDO.getEncryptPassword());
        return userModel;
    }

    private UserDO convertFromModel(UserModel userModel) {
        if (userModel == null) return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        userDO.setRegisterMode("telephone");
        return userDO;
    }

    private UserCreDO convertCreFromModel(UserModel userModel) {
        if (userModel == null) return null;
        UserCreDO userCreDO = new UserCreDO();
        userCreDO.setUserId(userModel.getId());
        userCreDO.setEncryptPassword(userModel.getEncryptedPassword());
        return userCreDO;
    }
}
