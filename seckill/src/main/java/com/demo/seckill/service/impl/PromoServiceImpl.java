package com.demo.seckill.service.impl;

import com.demo.seckill.entity.PromoDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.repository.PromoDOMapper;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.UserService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.PromoModel;
import com.demo.seckill.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {
    private final int NOT_START = 1;
    private final int IN_PROMOTION = 2;
    private final int ALREADY_END = 3;
    private PromoDOMapper promoDOMapper;
    private ItemService itemService;
    private RedisTemplate redisTemplate;
    private UserService userService;

    public PromoServiceImpl(PromoDOMapper promoDOMapper, ItemService itemService, RedisTemplate redisTemplate, UserService userService) {
        this.promoDOMapper = promoDOMapper;
        this.itemService = itemService;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
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
        //设置token发放计数器 - 设token总数为stock总数的2倍
        redisTemplate.opsForValue().set("promo_token_count_"+promoDO.getItemId(), itemModel.getStock()*2);
    }

    @Override
    public String generateSeckillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException {
        //售罄拦截 - check if sold out
        if (Boolean.TRUE.equals(redisTemplate.hasKey("promo_item_stock_invalid_" + itemId))) {
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        }

        //查验user
        UserModel userModel = this.userService.getUserById(userId);
        if (userModel == null) {
            return null;
        }
        //查验item & promo
        ItemModel itemModel = this.itemService.getItemById(itemId);
        if (itemModel == null || itemModel.getPromoModel() == null || itemModel.getPromoModel().getStatus() != this.IN_PROMOTION) {
            return null;
        }

        //检验是否还有token可以发放 - 直接减而不是查看，可以避免查完之后再减的脏读问题
        long result = this.redisTemplate.opsForValue().increment("promo_token_count_"+itemId, -1);
        if (result < 0) {
            return null;
        }

        //生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        //把token放入redis以备查验 - 设置5分钟有效期
        String key = "promo_token_"+promoId+"_"+itemId+"_"+userId;
        redisTemplate.opsForValue().set(key, token);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        return token;
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
            promoModel.setStatus(this.NOT_START);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            //已结束
            promoModel.setStatus(this.ALREADY_END);
        } else {
            //进行中，包含==
            promoModel.setStatus(this.IN_PROMOTION);
        }
        return promoModel;
    }
}
