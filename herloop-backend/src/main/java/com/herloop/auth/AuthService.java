package com.herloop.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.herloop.common.BusinessException;
import com.herloop.points.PointsService;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PointsService pointsService;

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

        // 处理邀请码
        if (StringUtils.hasText(req.getInviteCode())) {
            Long inviterId = parseInviteCode(req.getInviteCode());
            if (inviterId != null) {
                User inviter = userMapper.selectById(inviterId);
                if (inviter != null) {
                    user.setInvitedBy(inviterId);
                }
            }
        }

        userMapper.insert(user);

        // 注册送积分
        pointsService.changePoints(user.getId(), 100, "新用户注册奖励", "REGISTER", null);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return buildLoginResponse(token, user);
    }

    /**
     * 邀请好友注册完成首笔交易后，邀请人获得积分
     */
    public void onInviteeFirstTrade(Long inviteeId) {
        User invitee = userMapper.selectById(inviteeId);
        if (invitee == null || invitee.getInvitedBy() == null) return;

        Long inviterId = invitee.getInvitedBy();
        pointsService.changePoints(inviterId, 100, "邀请好友完成首笔交易奖励", "INVITE", inviteeId);
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

    private Long parseInviteCode(String code) {
        // 邀请码格式：user_{userId}，简单实现
        try {
            if (code.startsWith("user_")) {
                return Long.parseLong(code.substring(5));
            }
            return Long.parseLong(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LoginResponse buildLoginResponse(String token, User user) {
        LoginResponse.UserVO userVO = new LoginResponse.UserVO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getPoints(),
                user.getVerified(),
                user.getRole()
        );
        return new LoginResponse(token, userVO);
    }
}
