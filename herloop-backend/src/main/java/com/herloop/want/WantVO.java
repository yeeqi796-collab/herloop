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
    private String images;
    private String status;
    private LocalDateTime createdAt;
    private UserBrief user;

    @Data
    public static class UserBrief {
        private Long id;
        private String nickname;
        private String avatar;
        private Boolean verified;
    }
}
