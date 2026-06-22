package com.herloop.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.herloop.common.BusinessException;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest req) {
        // 检查邮箱是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (count > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setAvatar(req.getAvatar() != null ? req.getAvatar() : "🦢");
        user.setWechat(req.getWechat());
        user.setPoints(0);
        user.setVerified(false);
        user.setRole("USER");
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return buildLoginResponse(token, user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (user == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return buildLoginResponse(token, user);
    }

    private LoginResponse buildLoginResponse(String token, User user) {
        LoginResponse.UserVO userVO = new LoginResponse.UserVO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getPoints(),
                user.getVerified()
        );
        return new LoginResponse(token, userVO);
    }
}
