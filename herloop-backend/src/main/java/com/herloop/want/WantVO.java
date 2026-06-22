package com.herloop.want;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WantVO {

    private Long id;
    private String title;
    private String budget;
    private String description;
    private String icon;
    private String status;
    private LocalDateTime createdAt;
    private OwnerBrief owner;

    @Data
    public static class OwnerBrief {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
