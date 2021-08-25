package com.demo.seckill.repository;

import com.demo.seckill.entity.UserCreDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCreDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserCreDO record);

    int insertSelective(UserCreDO record);

    UserCreDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserCreDO record);

    int updateByPrimaryKey(UserCreDO record);

    UserCreDO selectByUserId(Integer userId);
}