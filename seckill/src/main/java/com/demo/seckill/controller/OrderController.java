package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.mq.Producer;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.ItemService;
import com.demo.seckill.service.OrderService;
import com.demo.seckill.service.PromoService;
import com.demo.seckill.service.model.UserModel;
import com.demo.seckill.util.CodeUtil;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.*;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin
public class OrderController extends BaseController {
    private OrderService orderService;
    private PromoService promoService;
    private RedisTemplate redisTemplate;
    private Producer producer;

    //用于浪涌流量的排队（削峰填谷）
    private ExecutorService executorService;

    //用于token bucket控制TPS
    private RateLimiter orderCreateRateLimiter;
    private final int rateLimit = 300;//safe TPS for one server

    @PostConstruct
    public void init(){
        //通过固定大小的线程池来控制队列泄洪,这里设置20个线程
        executorService = Executors.newFixedThreadPool(20);

        orderCreateRateLimiter = RateLimiter.create(rateLimit);
    }

    @Autowired
    public OrderController(OrderService orderService, RedisTemplate redisTemplate,
                           Producer producer, PromoService promoService) {
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
        this.producer = producer;
        this.promoService = promoService;
    }

    @GetMapping("/buy")
    public CommonReturnType buyItem(@RequestParam Integer itemId, @RequestParam Integer amount,
                                    @RequestParam String token, @RequestParam(required = false) Integer promoId,
                                    @RequestParam(required = false) String promoToken) throws BusinessException {
        //限制TPS流量
        if (orderCreateRateLimiter.tryAcquire()) {
            throw new BusinessException(EmBusinessError.RATELIMIT_NOT_ENOUGH);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        if (promoId != null) {//只有秒杀商品需要令牌，其他正常下单商品不需要令牌
            String seckillToken = (String) this.redisTemplate.opsForValue().get("promo_token_"+promoId+"_"+itemId+"_"+userModel.getId());
            if (seckillToken == null || !StringUtils.equals(seckillToken, promoToken)) {
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "Invalid Seckill Token");
            }
        }

        System.out.println(userModel.getId()+" is buying "+itemId + " for "+amount);

        //同步调用线程池的submit方法
        //congestion window为20的等待队列用来泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                //新建流水log用于追踪异步操作
                String stockLogId = orderService.initStockLog(itemId, amount);

                //直接用绑定的msg+local事务来代替单独的本地事务
                boolean success = producer.transactionAsyncReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId);

                if (!success) {
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "Order failed!");
                }
                return null;
            }
        });


        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }


        return CommonReturnType.create(null);
    }

    @GetMapping("/cancel/{id}")
    public CommonReturnType cancelOrder(@PathVariable String id) throws BusinessException {
        this.orderService.cancelOrder(id);
        return CommonReturnType.create(null);
    }

    //生成验证码, get这个url就会直接返回一个验证码img的url地址，不需要用subscribe来获取
    @GetMapping("/verifyCode")
    public void generateVerifyCode(@RequestParam String token, HttpServletResponse response) throws BusinessException, IOException {
        //检验用户是否登陆, 登陆的才发验证码
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "Not login, unable to generate verifyCode");
        }
        //生成验证码
        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        //将验证码与用户id进行绑定
        redisTemplate.opsForValue().set("verify_code_" + userModel.getId(), map.get("code"));
        redisTemplate.expire("verify_code_" + userModel.getId(), 5, TimeUnit.MINUTES);
        //将生成的验证码图片放入response输出流中
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
        System.out.println("验证码的值为：" + map.get("code"));

    }

    private boolean isValidCode(String codeToVerify, Integer userId) {
        String code = (String) redisTemplate.opsForValue().get("verify_code_" + userId);
        if (code == null || !StringUtils.equals(code, codeToVerify)) {
            return false;
        }
        return true;
    }

    @GetMapping("/seckillToken")
    public CommonReturnType generateSeckillToken(@RequestParam Integer itemId,
                                                 @RequestParam String token,
                                                 @RequestParam Integer promoId,
                                                 @RequestParam String code) throws BusinessException {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "Not login, unable to generate SeckillToken");
        }

        //检验验证码 - 防止恶意刷单
        if (!this.isValidCode(code, userModel.getId())) {
            throw new BusinessException(EmBusinessError.VERIFYCODE_NOT_VALID);
        }

        //获取seckill token
        String seckillToken = this.promoService.generateSeckillToken(promoId, itemId, userModel.getId());
        if (seckillToken == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "Fail to Generate Seckill Token");
        }

        return CommonReturnType.create(seckillToken);
    }
}
