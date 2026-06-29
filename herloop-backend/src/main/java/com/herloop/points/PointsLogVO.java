package com.herloop.points;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLogVO {

    private Long id;
    private String amount;
    private String description;
    private String sourceType;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
}
