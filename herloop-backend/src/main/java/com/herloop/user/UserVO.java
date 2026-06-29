package com.herloop.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private String wechat;
    private Integer points;
    private Boolean verified;
    private String role;
    private Integer productCount;
    private Integer tradeCount;
    private String inviteCode;
    private LocalDateTime createdAt;
}
