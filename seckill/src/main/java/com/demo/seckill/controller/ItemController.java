package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.ItemServiceImp;
import com.demo.seckill.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return null;
    }

    @PostMapping("/new")
    public CommonReturnType createItem(@RequestBody ItemModel itemModel) throws BusinessException {
        ItemModel item= this.itemServiceImp.createItem(itemModel);
        return CommonReturnType.create(this.convertFromModel(item));
    }

    private ItemVO convertFromModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        return itemVO;
    }

}
