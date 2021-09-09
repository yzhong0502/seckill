package com.demo.seckill.repository;

import com.demo.seckill.entity.ItemStockDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemStockDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ItemStockDO record);

    int insertSelective(ItemStockDO record);

    ItemStockDO selectByPrimaryKey(Integer id);

    ItemStockDO selectByItemId(Integer itemId);

    void updateStock(ItemStockDO record);

    int updateByPrimaryKeySelective(ItemStockDO record);

    int updateByPrimaryKey(ItemStockDO record);
}