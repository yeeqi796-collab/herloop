package com.herloop.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserVO user;

    @Data
    @AllArgsConstructor
    public static class UserVO {
        private Long id;
        private String email;
        private String nickname;
        private String avatar;
        private Integer points;
        private Boolean verified;
        private String role;
    }
}
