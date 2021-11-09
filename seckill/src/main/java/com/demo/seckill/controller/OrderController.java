package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.mq.Producer;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.OrderService;
import com.demo.seckill.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin
public class OrderController extends BaseController {
    private OrderService orderService;
    private RedisTemplate redisTemplate;
    private Producer producer;

    @Autowired
    public OrderController(OrderService orderService, RedisTemplate redisTemplate, Producer producer) {
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
        this.producer = producer;
    }

    @GetMapping("/buy")
    public CommonReturnType buyItem(@RequestParam Integer itemId, @RequestParam Integer amount, @RequestParam String token, @RequestParam(required = false) Integer promoId) throws BusinessException {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        //check if sold out
        if (redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)) {
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        }

        System.out.println(userModel.getId()+" is buying "+itemId + " for "+amount);

        //新建流水log用于追踪异步操作
        String stockLogId = this.orderService.initStockLog(itemId, amount);

        //直接用绑定的msg+local事务来代替单独的本地事务
        boolean success = producer.transactionAsyncReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId);

        if (!success) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "Order failed!");
        }
        return CommonReturnType.create(null);
    }

    @GetMapping("/cancel/{id}")
    public CommonReturnType cancelOrder(@PathVariable String id) throws BusinessException {
        this.orderService.cancelOrder(id);
        return CommonReturnType.create(null);
    }
}
