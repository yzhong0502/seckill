package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
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
    private HttpServletRequest httpServletRequest;
    private RedisTemplate redisTemplate;

    @Autowired
    public OrderController(OrderService orderService, HttpServletRequest httpServletRequest, RedisTemplate redisTemplate) {
        this.orderService = orderService;
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
        this.orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        return CommonReturnType.create(null);
    }

    @GetMapping("/cancel/{id}")
    public CommonReturnType cancelOrder(@PathVariable String id) throws BusinessException {
        this.orderService.cancelOrder(id);
        return CommonReturnType.create(null);
    }
}
