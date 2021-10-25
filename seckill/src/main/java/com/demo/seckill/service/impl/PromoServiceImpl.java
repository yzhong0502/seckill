package com.demo.seckill.service.impl;

import com.demo.seckill.entity.PromoDO;
import com.demo.seckill.repository.PromoDOMapper;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {
    private PromoDOMapper promoDOMapper;
    private ItemService itemService;
    private RedisTemplate redisTemplate;

    @Autowired
    public PromoServiceImpl(PromoDOMapper promoDOMapper, ItemService itemService, RedisTemplate redisTemplate) {
        this.promoDOMapper = promoDOMapper;
        this.itemService = itemService;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = this.promoDOMapper.selectByItemId(itemId);
        return this.convertFromDataObject(promoDO);
    }

    @Override
    public void publishPromo(Integer promoId) {
        //活动id获取活动
        PromoDO promoDO = this.promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0) {//无活动适用商品
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //将库存同步到redis
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());

    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) return null;
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime((promoDO.getEndDate())));
        //判断活动的status
        if (promoModel.getStartDate().isAfterNow()) {
            //还未开始
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            //已结束
            promoModel.setStatus(3);
        } else {
            //进行中，包含==
            promoModel.setStatus(2);
        }
        return promoModel;
    }
}
