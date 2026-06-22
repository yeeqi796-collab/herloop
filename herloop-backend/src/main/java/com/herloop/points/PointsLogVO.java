package com.herloop.points;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLogVO {

    private Long id;
    private Integer amount;
    private String description;
    private LocalDateTime createdAt;
}
