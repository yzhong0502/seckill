package com.demo.seckill.controller;

import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.ItemServiceImp;
import com.demo.seckill.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("item")
@RequestMapping("/item")
@CrossOrigin
public class ItemController extends BaseController {
    private ItemServiceImp itemServiceImp;

    @Autowired
    public ItemController(ItemServiceImp itemServiceImp) {
        this.itemServiceImp = itemServiceImp;
    }

    @GetMapping("/all")
    public CommonReturnType getAllItems() {
        List<ItemModel> itemModelList = this.itemServiceImp.listItem();
        List<ItemVO> list = itemModelList.stream().map(itemModel -> {
            return this.convertFromModel(itemModel);
        }).collect(Collectors.toList());
        return CommonReturnType.create(list);
    }

    @GetMapping("/get/{id}")
    public CommonReturnType getItem(@PathVariable Integer id) {
        ItemModel itemModel = this.itemServiceImp.getItemById(id);
        return CommonReturnType.create(this.convertFromModel(itemModel));
    }

    @GetMapping("/buy")
    @Transactional
    public CommonReturnType buyItem(@RequestParam Integer id) throws BusinessException{
        //check stock
        ItemModel itemModel = this.itemServiceImp.getItemById(id);
        if (itemModel.getStock() <= 0) {
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock() - 1);
        return CommonReturnType.create(null);
    }

    @PostMapping("/create")
    public CommonReturnType createItem(@RequestBody ItemModel itemModel, @RequestParam double price) throws BusinessException {
        System.out.println(itemModel.toString());
        itemModel.setSales(0);
        itemModel.setPrice(new BigDecimal(price));
        ItemModel item= this.itemServiceImp.createItem(itemModel);
        return CommonReturnType.create(this.convertFromModel(item));
    }

    private ItemVO convertFromModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        System.out.println(itemVO.toString());
        return itemVO;
    }

}
