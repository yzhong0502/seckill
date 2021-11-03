package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.CacheService;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("item")
@RequestMapping("/item")
@CrossOrigin
public class ItemController extends BaseController {
    private ItemService itemService;
    private PromoService promoService;
    private RedisTemplate redisTemplate;
    private CacheService cacheService;

    @Autowired
    public ItemController(ItemService itemService, PromoService promoService, RedisTemplate redisTemplate, CacheService cacheService) {
        this.itemService = itemService;
        this.promoService = promoService;
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
    }

    @GetMapping("/publish")
    public CommonReturnType publishPromo(@RequestParam Integer promoId){
        this.promoService.publishPromo(promoId);
        return CommonReturnType.create(null);
    }

    @GetMapping("/all")
    public CommonReturnType getAllItems() {
        List<ItemModel> itemModelList = this.itemService.listItem();
        List<ItemVO> list = itemModelList.stream().map(itemModel -> {
            return this.convertFromModel(itemModel);
        }).collect(Collectors.toList());
        return CommonReturnType.create(list);
    }

    @GetMapping("/get/{id}")
    public CommonReturnType getItem(@PathVariable Integer id) {
        String key = "item_" + id;
        //1 查看本地缓存
        ItemModel itemModel = (ItemModel) this.cacheService.getFromCommonCache(key);
        if (itemModel == null) {
            //2 查看redis中是否已经有信息
            itemModel = (ItemModel) this.redisTemplate.opsForValue().get(key);
            if (itemModel == null) {
                //如果没有，从database取出并存入redis。需要设置expire时间
                itemModel = this.itemService.getItemById(id);
                this.redisTemplate.opsForValue().set(key, itemModel);
                this.redisTemplate.expire(key, 10, TimeUnit.MINUTES);
            }
            //填充本地缓存
            this.cacheService.setCommonCache(key, itemModel);
        }

        return CommonReturnType.create(this.convertFromModel(itemModel));
    }

    @PostMapping("/create")
    public CommonReturnType createItem(@RequestBody ItemModel itemModel, @RequestParam double price) throws BusinessException {
        System.out.println(itemModel.toString());
        itemModel.setSales(0);
        itemModel.setPrice(new BigDecimal(price));
        ItemModel item= this.itemService.createItem(itemModel);
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
