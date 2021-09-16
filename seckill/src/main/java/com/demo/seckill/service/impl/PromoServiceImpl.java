package com.demo.seckill.service.impl;

import com.demo.seckill.entity.PromoDO;
import com.demo.seckill.repository.PromoDOMapper;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {
    private PromoDOMapper promoDOMapper;

    @Autowired
    public PromoServiceImpl(PromoDOMapper promoDOMapper) {
        this.promoDOMapper = promoDOMapper;
    }


    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = this.promoDOMapper.selectByItemId(itemId);
        return this.convertFromDataObject(promoDO);
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
