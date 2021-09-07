package com.demo.seckill.service.model;

import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemModel {

    private Integer id;

    @NotBlank(message="Title can't blank")
    private String title;

    @NotNull(message="price can't be null")
    @Min(value=0, message="price must be above 0")
    private BigDecimal price;//用BigDecimal避免精度问题，与DO类型不同需要手动设置

    @NotNull(message="stock can't be null")
    @Min(value=0, message="stock must be above 0")
    private Integer stock;

    @NotBlank(message = "description can't be blank")
    private String description;

    private Integer sales;

    @NotBlank(message="must have image Url")
    private String imgUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
