package com.demo.seckill.service;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.service.model.OrderModel;

import java.util.List;

public interface OrderService {
    List<OrderModel> getAllOrders();
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;

    void cancelOrder(String id) throws BusinessException;
}
