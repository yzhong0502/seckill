package com.demo.seckill.repository;

import com.demo.seckill.entity.SeqDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeqDOMapper {
    int deleteByPrimaryKey(String name);

    int insert(SeqDO record);

    int insertSelective(SeqDO record);

    SeqDO getSequenceByName(String name);

    int updateByPrimaryKeySelective(SeqDO record);

    int updateByPrimaryKey(SeqDO record);
}