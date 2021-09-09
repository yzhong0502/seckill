package com.demo.seckill.service.impl;

import com.demo.seckill.entity.ItemDO;
import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.entity.OrderDO;
import com.demo.seckill.entity.UserDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.repository.ItemDOMapper;
import com.demo.seckill.repository.ItemStockDOMapper;
import com.demo.seckill.repository.OrderDOMapper;
import com.demo.seckill.repository.UserDOMapper;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.OrderService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.OrderModel;
import com.demo.seckill.service.model.UserModel;
import com.demo.seckill.validator.ValidatorImp;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {
    private OrderDOMapper orderDOMapper;
    private UserServiceImpl userServiceImpl;
    private ItemServiceImp itemServiceImp;
    private ValidatorImp validator;

    @Autowired


    @Override
    public List<OrderModel> getAllOrders() {
        return this.orderDOMapper.selectAll().stream().map(orderDO -> {
            return this.convertFromOrderDO(orderDO);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException{
        //1.校验下单状态：用户是否合法，商品是否存在，购买数量是否正确
        UserModel userModel = this.userServiceImpl.getUserById(userId);
        if (userModel == null) throw new BusinessException(EmBusinessError.USER_NOT_EXIST, "User not valid");
        ItemModel itemModel = this.itemServiceImp.getItemById(itemId);
        if (itemModel == null) throw new BusinessException(EmBusinessError.ITEM_NOT_FOUND, "Item not valid");
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Amount not valid");
        }
        //2.落单减库存
        if (!this.itemServiceImp.decreaseStock(itemId, amount)) {
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        } else {
            //3.订单入库
            OrderDO orderDO = new OrderDO();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            orderDO.setId(sdf.format(new Date()));
            orderDO.setUserId(userId);
            orderDO.setItemId(itemId);
            orderDO.setItemPrice(itemModel.getPrice().doubleValue());
            orderDO.setAmount(amount);
            orderDO.setOrderPrice(amount * itemModel.getPrice().doubleValue());
            this.orderDOMapper.insertSelective(orderDO);
            return this.convertFromOrderDO(orderDO);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String id) throws BusinessException {
        OrderDO orderDO = this.orderDOMapper.selectByPrimaryKey(id);
        if (orderDO == null) {
            throw new BusinessException(EmBusinessError.ORDER_NOT_FOUND);
        } else {
            this.orderDOMapper.deleteByPrimaryKey(id);
        }
    }

    private OrderModel convertFromOrderDO(OrderDO orderDO) {
        if (orderDO == null) return null;
        OrderModel orderModel = new OrderModel();
        BeanUtils.copyProperties(orderDO, orderModel);
        orderModel.setOrderPrice(new BigDecimal(orderDO.getOrderPrice()));
        orderModel.setItemPrice(new BigDecimal(orderDO.getItemPrice()));
        return orderModel;
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        return orderDO;
    }
}
