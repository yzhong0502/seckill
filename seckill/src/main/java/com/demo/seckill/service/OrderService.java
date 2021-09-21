package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.OrderModel;

import java.util.List;

public interface OrderService {
    List<OrderModel> getAllOrders();

    //两种方式
    //1。通过前端url获得promoId然后下单接口内校验对应id是否属于对应商品且活动在进行中
    //2。直接在下单接口内判断对应商品是否存在promo活动，若存在则以promo price下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;

    void cancelOrder(String id) throws BusinessException;
}
