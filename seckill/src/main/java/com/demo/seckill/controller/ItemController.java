package com.demo.seckill.controller;

import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.ItemServiceImp;
import com.demo.seckill.service.impl.PromoServiceImpl;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
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
    private PromoServiceImpl promoService;

    @Autowired
    public ItemController(ItemServiceImp itemServiceImp, PromoServiceImpl promoService) {
        this.itemServiceImp = itemServiceImp;
        this.promoService = promoService;
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
        PromoModel promoModel = itemModel.getPromoModel();
        if (promoModel != null) {
            itemVO.setPromoId(promoModel.getId());
            itemVO.setPromoStatus(promoModel.getStatus());
            itemVO.setPromoPrice(promoModel.getPromoItemPrice());
            itemVO.setStartDate(promoModel.getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

}
