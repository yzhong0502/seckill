package com.demo.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.demo.seckill.entity.StockLogDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.repository.StockLogDOMapper;
import com.demo.seckill.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class Producer {
    private TransactionMQProducer producer;

    private OrderService orderService;



    //从application.properties注入值
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    public Producer(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct//在construct之后触发，常用于设置
    public void init() throws MQClientException {
        //mq producer初始化
        producer = new TransactionMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.setTransactionListener(new TransactionListener() {
            @Override//需要与message绑定的本地事务
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                String stockLogId = (String) ((Map<String, Object>)arg).get("stockLogId");
                try {
                    //在createOrder中更新log状态
                    orderService.createOrder((Integer) ((Map<String, Object>)arg).get("userId"),
                            (Integer)((Map<String, Object>)arg).get("itemId"),
                            (Integer)((Map<String, Object>)arg).get("promoId"),
                            (Integer)((Map<String, Object>)arg).get("amount"),
                            stockLogId);
                    //可能出现问题：如果createOrder卡住了无法往下进行stock log就会一致是init状态，需要加入超时释放功能
                } catch (BusinessException e) {
                    e.printStackTrace();
                    //update log status = rollback
                    orderService.updateStockLog(stockLogId, false);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                //根据是否扣减库存成功来判断要返回的commit，rollback还是继续unknown
                String jsonString = new String(msg.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                String stockLogId = (String) map.get("stockLogId");
                int status = orderService.getStockLogStatus(stockLogId);
                if (status == 2) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                if (status == 1 || status == -1) {
                    return LocalTransactionState.UNKNOW;
                }

                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
        //launch the instance
        producer.start();
    }

    //事务型同步库存扣减消息
    public boolean transactionAsyncReduceStock(Integer userId, Integer promoId, Integer itemId, Integer amount, String stockLogId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);
        Message message = new Message(topicName, "increase",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        argsMap.put("stockLogId", stockLogId);

        TransactionSendResult transactionSendResult = null;

        try {
            transactionSendResult = producer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (transactionSendResult != null && transactionSendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        }
        return false;
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
