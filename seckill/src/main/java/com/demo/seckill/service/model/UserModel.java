package com.demo.seckill.service.model;

import org.apache.tomcat.util.security.MD5Encoder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

//核心领域模型
public class UserModel implements Serializable {
    private Integer id;
    @NotBlank(message="Name can't be empty!")
    private String name;

    private Byte gender;
    @NotNull(message = "Must input age!")
    @Min(value=1, message="Age must be greater than 0")
    @Max(value=149, message="Age must be less than 150")
    private Integer age;
    @NotBlank(message="Must input telephone!")
    private String telphone;
    @NotBlank(message = "Must input register mode!")
    private String registerMode;
    @NotBlank(message = "Must input address!")
    private String address;

    private String encryptedPassword;

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptedPassword() {
        return this.encryptedPassword;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone == null ? null : telphone.trim();
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode == null ? null : registerMode.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }


    public void setEncryptPassword(String encryptPassword) {
        this.encryptedPassword = encryptPassword;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                ", telphone='" + telphone + '\'' +
                ", registerMode='" + registerMode + '\'' +
                ", address='" + address + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                '}';
    }
}
