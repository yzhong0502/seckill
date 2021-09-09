package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;
}
