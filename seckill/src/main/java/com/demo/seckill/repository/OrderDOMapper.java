package com.demo.seckill.repository;

import com.demo.seckill.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDOMapper {
    int deleteByPrimaryKey(String id);

    int insert(OrderDO record);

    int insertSelective(OrderDO record);

    OrderDO selectByPrimaryKey(String id);

    List<OrderDO> selectAll();

    int updateByPrimaryKeySelective(OrderDO record);

    int updateByPrimaryKey(OrderDO record);
}