package com.demo.seckill.service.impl;

import com.demo.seckill.entity.ItemDO;
import com.demo.seckill.entity.ItemStockDO;
import com.demo.seckill.entity.StockLogDO;
import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.mq.Producer;
import com.demo.seckill.repository.ItemDOMapper;
import com.demo.seckill.repository.ItemStockDOMapper;
import com.demo.seckill.repository.StockLogDOMapper;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.model.ItemModel;
import com.demo.seckill.service.model.PromoModel;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.validator.ValidationResult;
import com.demo.seckill.validator.ValidatorImp;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImp implements ItemService {
    private ItemDOMapper itemDOMapper;
    private ItemStockDOMapper itemStockDOMapper;
    private ValidatorImp validator;
    private PromoService promoService;
    private RedisTemplate redisTemplate;


    @Autowired
    public ItemServiceImp(ItemDOMapper itemDOMapper, ItemStockDOMapper itemStockDOMapper, ValidatorImp validator, @Lazy PromoService promoService, RedisTemplate redisTemplate) {
        this.itemDOMapper = itemDOMapper;
        this.itemStockDOMapper = itemStockDOMapper;
        this.validator = validator;
        this.promoService = promoService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional//需要在多个表中操作
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //转化model -> data object
        ItemDO itemDO = this.convertFromModelToItem(itemModel);
        //写入数据库
        try {
            this.itemDOMapper.insertSelective(itemDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Telephone already exists!");
        }
        itemModel.setId(itemDO.getId());
        if (itemModel.getId() == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemStockDO itemStockDO = this.convertFromModelToStock(itemModel);
        this.itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> list = this.itemDOMapper.selectAll();
        List<ItemModel> itemModelList = list.stream().map(itemDO -> {
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertFromDataObject(itemDO, itemStockDO);
            PromoModel promoModel = this.promoService.getPromoByItemId(itemDO.getId());
            if (promoModel != null && promoModel.getStatus().intValue() != 3) {
                itemModel.setPromoModel(promoModel);
            }
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override //read doesn't need roll back so no need @Transactional
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = this.itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) return null;
        //获得商品库存
        ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(id);

        ItemModel itemModel = this.convertFromDataObject(itemDO, itemStockDO);

        //获得商品活动信息
        PromoModel promoModel = this.promoService.getPromoByItemId(itemDO.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemByIdFromCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_"+id);//区别于详情页
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id, itemModel);
            redisTemplate.expire("item_validate_"+id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    @Transactional//多个操作都需要加，查询操作也需要一致
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        //扣减库存缓存化: 需要提前进行发布
        long leftStock = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*-1);
        //int affectedRow = this.itemStockDOMapper.decreaseStock(itemId, amount);
        if (leftStock > 0) {
            return true;
            //not success add stock back
            //redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        } else if (leftStock == 0) {
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId, "true");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        this.itemDOMapper.increaseSales(itemId, amount);
    }


    private ItemDO convertFromModelToItem(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemDO itemDo = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDo);
        itemDo.setPrice(itemModel.getPrice().doubleValue());
        return itemDo;
    }

    private ItemStockDO convertFromModelToStock(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }

    private ItemModel convertFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        if (itemDO == null) return null;
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }

}
