package com.demo.seckill.service;

import com.demo.seckill.service.model.PromoModel;

public interface PromoService {
    //获取即将开始或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);
}
