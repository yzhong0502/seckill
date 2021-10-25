package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.UserService;
import com.demo.seckill.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController("user")
@RequestMapping("/user")
@CrossOrigin
public class UserController extends BaseController {
    private UserService userService;
    private HttpServletRequest httpServletRequest;
    private RedisTemplate redisTemplate;
    private String otp = "";

    @Autowired
    public UserController(UserService userService, HttpServletRequest httpServletRequest, RedisTemplate redisTemplate) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/get")
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException{
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        return CommonReturnType.create(convertFromModelObject(userModel), "success");
    }

    //用户获取otp短信接口
    @PostMapping("/otp")
    public CommonReturnType getOtp(@RequestBody String telphone) {
        //按照一定规则生成otp验证码
        int randomInt = new Random().nextInt(10000);
        randomInt += 10000;//此时随机数取值为10000 ～ 19999
        String otpCode = String.valueOf(randomInt);

        //将验证码与用户手机号相关联.最好使用redis。此处为方便使用HttpSession
        this.otp = otpCode;
        //将otp验证码通过短信发送给用户，此处省略
        System.out.println("telephone = " + telphone + " & otpcode = " + otpCode);
        return CommonReturnType.create(null);
    }

    //验证otp接口
    @GetMapping("/verify")
    public CommonReturnType verifyOTP(@RequestParam String phone, @RequestParam String otp) throws BusinessException {
        if (otp.equals(this.otp)) {
            return CommonReturnType.create(null);
        } else {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "Validation failed");
        }
    }

    @PostMapping("/register")
    public CommonReturnType register(@RequestBody UserModel userModel) throws BusinessException {
        System.out.println(userModel.toString());
        userModel = this.userService.register(userModel);
        //生成登陆凭证token，uuid
        String uuidToken = getToken();
        //建立token和用户登陆状态之间的联系
        redisTemplate.opsForValue().set(uuidToken, userModel);
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);
        //下发token
        return CommonReturnType.create(uuidToken);
    }

    private UserVO convertFromModelObject(UserModel userModel) {
        if (userModel == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

    @PostMapping("/login")
    public CommonReturnType login(@RequestBody Map<String,String> data) throws BusinessException {
        UserModel userModel = this.userService.login(data.get("telphone"), data.get("password"));
        //若用户登陆成功将对应对登陆信息和登陆凭证一起存入redis
        //生成登陆凭证token，uuid
        String uuidToken = getToken();
        //建立token和用户登陆状态之间的联系
        redisTemplate.opsForValue().set(uuidToken, userModel);
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);
        //下发token
        return CommonReturnType.create(uuidToken);
    }

    private String getToken() {
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");
        return uuidToken;
    }

}
