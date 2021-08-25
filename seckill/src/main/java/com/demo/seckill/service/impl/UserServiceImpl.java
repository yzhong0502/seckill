package com.demo.seckill.service.impl;

import com.demo.seckill.entity.UserCreDO;
import com.demo.seckill.entity.UserDO;
import com.demo.seckill.repository.UserCreDOMapper;
import com.demo.seckill.repository.UserDOMapper;
import com.demo.seckill.service.UserService;
import com.demo.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private UserModel convertFromDataObject(UserDO userDO, UserCreDO userCreDO) {
        if (userDO == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userCreDO != null) userModel.setEncryptPassword(userCreDO.getEncryptPassword());
        return userModel;
    }
}
