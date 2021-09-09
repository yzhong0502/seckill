package com.demo.seckill.controller;

import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.OrderServiceImp;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin
public class OrderController extends BaseController {
    private OrderServiceImp orderServiceImp;

    @Autowired
    public OrderController(OrderServiceImp orderServiceImp) {
        this.orderServiceImp = orderServiceImp;
    }

    @PostMapping("/buy")
    public CommonReturnType buyItem(@RequestParam Integer userId, @RequestParam Integer itemId, @RequestParam Integer amount) throws BusinessException {
        OrderModel orderModel = this.orderServiceImp.createOrder(userId, itemId, amount);
        return CommonReturnType.create(orderModel);
    }

    @GetMapping("/cancel/{id}")
    public CommonReturnType cancelOrder(@PathVariable String id) throws BusinessException {
        this.orderServiceImp.cancelOrder(id);
        return CommonReturnType.create(null);
    }
}
