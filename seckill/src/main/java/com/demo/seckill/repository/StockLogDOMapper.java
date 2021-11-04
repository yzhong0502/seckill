package com.demo.seckill.repository;

import com.demo.seckill.entity.StockLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockLogDOMapper {
    int deleteByPrimaryKey(String stockLogId);

    int insert(StockLogDO record);

    int insertSelective(StockLogDO record);

    StockLogDO selectByPrimaryKey(String stockLogId);

    int updateByPrimaryKeySelective(StockLogDO record);

    int updateByPrimaryKey(StockLogDO record);
}