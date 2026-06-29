package com.herloop.product;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String category;

    @TableField("condition_desc")
    private String conditionDesc;

    private String description;

    private BigDecimal cashPrice;

    private Integer pointsPrice;

    private String tradeMode;

    private String status;

    private String icon;

    private String images;

    private String wechat;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
