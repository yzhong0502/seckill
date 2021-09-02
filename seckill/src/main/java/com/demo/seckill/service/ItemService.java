package com.demo.seckill.service;

import com.demo.seckill.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel);

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);
}
