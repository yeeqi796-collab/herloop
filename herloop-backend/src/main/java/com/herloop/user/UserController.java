package com.herloop.user;

import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        Long userId = CurrentUser.getId();
        User user = userMapper.selectById(userId);
        return Result.success(toVO(user));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setWechat(user.getWechat());
        vo.setPoints(user.getPoints());
        vo.setVerified(user.getVerified());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
