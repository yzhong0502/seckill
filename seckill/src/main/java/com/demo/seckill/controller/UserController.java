package com.demo.seckill.controller;

import com.demo.seckill.entity.UserDO;
import com.demo.seckill.repository.UserDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController("user")
@RequestMapping("/user")
public class UserController {

    private final UserDOMapper userDOMapper;

    @Autowired
    public UserController(UserDOMapper userDOMapper) {
        this.userDOMapper = userDOMapper;
    }

    @RequestMapping("/")
    public String home() {
        UserDO userDo = userDOMapper.selectByPrimaryKey(1);
        if(userDo == null) {
            return "No such user.";
        } else {
            return userDo.getName();
        }
    }

    @RequestMapping("/get")
    public void getUser(@RequestParam(name="id") Integer id) {

    }

}
