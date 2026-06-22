package com.herloop.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verification_proof")
public class VerificationProof {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String imageUrl;

    private String status;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
