package com.demo.seckill.service.impl;

import com.demo.seckill.entity.*;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.mq.Producer;
import com.demo.seckill.repository.*;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.OrderService;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.UserService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {
    private OrderDOMapper orderDOMapper;
    private UserService userService;
    private ItemService itemService;
    private ValidatorImp validator;
    private SeqDOMapper seqDOMapper;
    private PromoService promoService;
    private StockLogDOMapper stockLogDOMapper;


    @Autowired
    public OrderServiceImp(OrderDOMapper orderDOMapper, UserService userService,
                           ItemService itemService,
                           ValidatorImp validator, SeqDOMapper seqDOMapper, StockLogDOMapper stockLogDOMapper) {
        this.orderDOMapper = orderDOMapper;
        this.userService = userService;
        this.itemService = itemService;
        this.validator = validator;
        this.seqDOMapper = seqDOMapper;
        this.stockLogDOMapper = stockLogDOMapper;
    }

    @Override
    public List<OrderModel> getAllOrders() {
        return this.orderDOMapper.selectAll().stream().map(orderDO -> {
            return this.convertFromOrderDO(orderDO);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException{
        //????????????????????????????????????????????????????????????, ?????????????????? ??????seckill token????????????
        ItemModel itemModel = this.itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.ITEM_NOT_FOUND);
        }
        //1. ??????????????????????????????
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Amount not valid");
        }

        //2.???????????????
        if (!this.itemService.decreaseStock(itemId, amount)) {
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        } else {
            //3.???????????????
            this.itemService.increaseSales(itemId, amount);
            //4.????????????
            //???????????????log
            if (!updateStockLog(stockLogId, true)) {
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "Update log status failed.");
            }

            OrderModel orderModel = new OrderModel();
            orderModel.setUserId(userId);
            orderModel.setItemId(itemId);
            if (promoId != null) {
                orderModel.setPromoId(promoId);
                orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
            } else {
                orderModel.setItemPrice(itemModel.getPrice());
            }
            orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
            orderModel.setAmount(amount);
            //?????????????????????
            orderModel.setId(this.generateOrderNo());

            this.orderDOMapper.insertSelective(this.convertFromOrderModel(orderModel));
            return orderModel;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)//???????????????????????????????????????????????????????????????????????????????????????????????????
    String generateOrderNo() {
        StringBuilder sb = new StringBuilder();
        //??????????????????16???
        //???8?????????????????????yyyyMMdd?????????????????????????????????
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        sb.append(nowDate);

        //??????6?????????????????????????????????????????????????????????????????????
        //?????????database table?????????????????????
        SeqDO seqDO = this.seqDOMapper.getSequenceByName("order_info");
        int sequence = seqDO.getCurrentValue();
        seqDO.setCurrentValue(seqDO.getCurrentValue() + seqDO.getStep());
        this.seqDOMapper.updateByPrimaryKeySelective(seqDO);
        String sequenceString = String.valueOf(sequence);
        for (int i = 0, n = sequenceString.length(); i < 6 - n; ++i) {
            sb.append("0");
        }
        sb.append(sequenceString);
        //????????????????????????????????????????????????????????????distributed system???
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

    @Override
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        //??????????????????????id?????????increment id
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStatus(1);//????????????
        this.stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

    @Override
    public boolean updateStockLog(String stock_log_id, boolean success) {
        StockLogDO stockLogDO = this.stockLogDOMapper.selectByPrimaryKey(stock_log_id);
        if (stockLogDO != null && stockLogDO.getStatus() == 1) {
            stockLogDO.setStatus(success? 2 : 3);
            int affectedRow = this.stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
            return affectedRow > 0;
        }
        return false;
    }

    @Override
    public int getStockLogStatus(String stockLogId) {
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO == null) return -1;
        return stockLogDO.getStatus();
    }


}
