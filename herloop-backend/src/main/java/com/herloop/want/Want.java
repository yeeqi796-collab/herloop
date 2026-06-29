package com.herloop.want;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("want")
public class Want {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String budget;

    private String description;

    private String icon;

    private String images;

    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
