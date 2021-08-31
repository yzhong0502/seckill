package com.demo.seckill.service.impl;

import com.demo.seckill.entity.UserCreDO;
import com.demo.seckill.entity.UserDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.repository.UserCreDOMapper;
import com.demo.seckill.repository.UserDOMapper;
import com.demo.seckill.service.UserService;
import com.demo.seckill.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private UserDOMapper userDOMapper;
    private UserCreDOMapper userCreDOMapper;

    @Autowired
    public UserServiceImpl(UserDOMapper userDOMapper, UserCreDOMapper userCreDOMapper) {
        this.userCreDOMapper = userCreDOMapper;
        this.userDOMapper = userDOMapper;
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
    public void register(UserModel userModel) throws BusinessException{
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(userModel.getName())
            || userModel.getGender() == null
            || userModel.getAge() == null
            || StringUtils.isEmpty(userModel.getAddress())
            || StringUtils.isEmpty(userModel.getTelphone())) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserDO userDO = this.convertFromModel(userModel);
        int id = this.userDOMapper.insertSelective(userDO);


        UserCreDO userCreDO = this.convertCreFromModel(userModel);
        this.userCreDOMapper.insert(userCreDO);

        return;

    }

    @Override
    public void login(String telphone, String password) {

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
