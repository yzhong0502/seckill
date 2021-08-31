package com.demo.seckill.controller;

import com.demo.seckill.error.BusinessException;
import com.demo.seckill.error.EmBusinessError;
import com.demo.seckill.response.CommonReturnType;
import com.demo.seckill.service.impl.UserServiceImpl;
import com.demo.seckill.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Random;


@RestController("user")
@RequestMapping("/user")
@CrossOrigin
public class UserController extends BaseController {
    private UserServiceImpl userService;
    private HttpServletRequest httpServletRequest;

    @Autowired
    public UserController(UserServiceImpl userService, HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
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
        httpServletRequest.getSession().setAttribute(telphone, otpCode);
        System.out.println(httpServletRequest.getSession().getAttribute(telphone));
        //将otp验证码通过短信发送给用户，此处省略
        System.out.println("telephone = " + telphone + " & otpcode = " + otpCode);
        return CommonReturnType.create(null);
    }

    //验证otp接口
    @GetMapping("/verify")
    public CommonReturnType verifyOTP(@RequestParam String phone, @RequestParam String otp) throws BusinessException {
        System.out.println(phone);
        System.out.println(otp);
        String inSessionOtp = (String) httpServletRequest.getSession().getAttribute(phone);
        System.out.println(inSessionOtp);
        System.out.println(otp.equals(inSessionOtp));
        if (otp.equals(inSessionOtp)) {
            return CommonReturnType.create(null);
        } else {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "Validation failed");
        }
    }

    @PostMapping("/register")
    public CommonReturnType register(@RequestBody String name,
                                     @RequestBody byte gender,
                                     @RequestBody int age,
                                     @RequestBody String address,
                                     @RequestBody String password,
                                     @RequestBody String telphone) throws BusinessException {
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        userModel.setAddress(address);
        userModel.setEncryptedPassword(MD5Encoder.encode(password.getBytes(StandardCharsets.UTF_8)));
        userModel.setRegisterMode("telephone");
        userModel.setTelphone(telphone);
        this.userService.register(userModel);
        return CommonReturnType.create(null);
    }

    private UserVO convertFromModelObject(UserModel userModel) {
        if (userModel == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

}
