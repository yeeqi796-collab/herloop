package com.herloop.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "成色不能为空")
    private String condition;

    private String description;

    private BigDecimal cashPrice;

    private Integer pointsPrice;

    @NotBlank(message = "交易方式不能为空")
    private String tradeMode;

    private String icon;

    private String images;

    private String wechat;
}
