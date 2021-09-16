package com.demo.seckill.service.impl;

import com.demo.seckill.entity.*;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.repository.*;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.OrderService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.OrderModel;
import com.demo.seckill.service.model.UserModel;
import com.demo.seckill.validator.ValidatorImp;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {
    private OrderDOMapper orderDOMapper;
    private UserServiceImpl userServiceImpl;
    private ItemServiceImp itemServiceImp;
    private ValidatorImp validator;
    private SeqDOMapper seqDOMapper;

    @Autowired
    public OrderServiceImp(OrderDOMapper orderDOMapper, UserServiceImpl userServiceImpl,
                           ItemServiceImp itemServiceImp,
                           ValidatorImp validator, SeqDOMapper seqDOMapper) {
        this.orderDOMapper = orderDOMapper;
        this.userServiceImpl = userServiceImpl;
        this.itemServiceImp = itemServiceImp;
        this.validator = validator;
        this.seqDOMapper = seqDOMapper;
    }

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
            //3.支付加销量
            this.itemServiceImp.increaseSales(itemId, amount);
            //4.订单入库
            OrderModel orderModel = new OrderModel();
            orderModel.setUserId(userId);
            orderModel.setItemId(itemId);
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
            orderModel.setAmount(amount);
            orderModel.setOrderPrice(itemModel.getPromoModel().getPromoItemPrice().multiply(new BigDecimal(amount)));
            //生成交易订单号
            orderModel.setId(this.generateOrderNo());
            this.orderDOMapper.insertSelective(this.convertFromOrderModel(orderModel));
            return orderModel;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)//子事务不管总事务是否失败，只要提交掉就不会回滚（对子事务新建事务）
    String generateOrderNo() {
        StringBuilder sb = new StringBuilder();
        //订单号设计为16位
        //前8位位时间信息，yyyyMMdd。加入时间信息便于归档
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        sb.append(nowDate);

        //中间6位为自增序列，用于保证同一时间下的订单号不重复
        //用一个database table来管理自增序列
        SeqDO seqDO = this.seqDOMapper.getSequenceByName("order_info");
        int sequence = seqDO.getCurrentValue();
        seqDO.setCurrentValue(seqDO.getCurrentValue() + seqDO.getStep());
        this.seqDOMapper.updateByPrimaryKeySelective(seqDO);
        String sequenceString = String.valueOf(sequence);
        for (int i = 0, n = sequenceString.length(); i < 6 - n; ++i) {
            sb.append("0");
        }
        sb.append(sequenceString);
        //最后两位为分库表位，便于以后拿来拆分表（distributed system）
        sb.append("00");
        return sb.toString();
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
