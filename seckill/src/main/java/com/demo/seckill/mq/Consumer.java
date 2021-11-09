package com.demo.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.demo.seckill.repository.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Component
public class Consumer {
    private DefaultMQPushConsumer consumer;
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    public Consumer(ItemStockDOMapper itemStockDOMapper) {
        this.itemStockDOMapper = itemStockDOMapper;
    }

    //从application.properties注入值
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("Reduce_stock_consumer");
        consumer.setNamesrvAddr(nameAddr);
        //订阅topicName主题下的所有消息
        consumer.subscribe(topicName, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                Message msg = msgs.get(0);
                String jsonString = new String(msg.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                //实现库存真正到数据库扣减的逻辑
                itemStockDOMapper.decreaseStock(itemId, amount);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

    @PreDestroy
    public void shutdown() {
        consumer.shutdown();
    }
}
