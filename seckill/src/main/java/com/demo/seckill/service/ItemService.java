package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.ItemModel;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    //item及promo model缓存模型
    ItemModel getItemByIdFromCache(Integer id);

    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
