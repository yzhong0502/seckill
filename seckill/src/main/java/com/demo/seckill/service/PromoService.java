package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.PromoModel;

public interface PromoService {
    //获取即将开始或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //发布活动
    void publishPromo(Integer promoId);

    //生成秒杀token - 把验证user及item的步骤也放到token生成逻辑里面
    String generateSeckillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException;
}
