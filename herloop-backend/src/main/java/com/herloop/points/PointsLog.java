package com.herloop.points;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer amount;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
