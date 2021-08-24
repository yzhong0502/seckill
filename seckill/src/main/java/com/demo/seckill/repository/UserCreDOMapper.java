package com.demo.seckill.repository;

import com.demo.seckill.entity.UserCreDO;

public interface UserCreDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserCreDO record);

    int insertSelective(UserCreDO record);

    UserCreDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserCreDO record);

    int updateByPrimaryKey(UserCreDO record);
}