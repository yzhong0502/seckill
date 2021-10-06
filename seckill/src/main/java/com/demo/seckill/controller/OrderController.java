package com.demo.seckill.controller;

import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.OrderServiceImp;
import com.demo.seckill.service.impl.PromoServiceImpl;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.OrderModel;
import com.demo.seckill.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin
public class OrderController extends BaseController {
    private OrderServiceImp orderServiceImp;
    private HttpServletRequest httpServletRequest;
    private RedisTemplate redisTemplate;

    @Autowired
    public OrderController(OrderServiceImp orderServiceImp, HttpServletRequest httpServletRequest, RedisTemplate redisTemplate) {
        this.orderServiceImp = orderServiceImp;
        this.httpServletRequest = httpServletRequest;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/buy")
    public CommonReturnType buyItem(@RequestParam Integer itemId, @RequestParam Integer amount, @RequestParam String token, @RequestParam(required = false) Integer promoId) throws BusinessException {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        System.out.println(userModel.getId()+" is buying "+itemId + " for "+amount);
        this.orderServiceImp.createOrder(userModel.getId(), itemId, promoId, amount);
        return CommonReturnType.create(null);
    }

    @GetMapping("/cancel/{id}")
    public CommonReturnType cancelOrder(@PathVariable String id) throws BusinessException {
        this.orderServiceImp.cancelOrder(id);
        return CommonReturnType.create(null);
    }
}
