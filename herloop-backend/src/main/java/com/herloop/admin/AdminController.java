package com.herloop.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.herloop.auth.VerificationProof;
import com.herloop.auth.VerificationProofMapper;
import com.herloop.common.BusinessException;
import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import com.herloop.notification.NotificationService;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import com.herloop.trade.Trade;
import com.herloop.trade.TradeMapper;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VerificationProofMapper proofMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final TradeMapper tradeMapper;
    private final NotificationService notificationService;

    @GetMapping("/pending-proofs")
    public Result<List<ProofVO>> pendingProofs() {
        checkAdmin();

        LambdaQueryWrapper<VerificationProof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationProof::getStatus, "PENDING");
        wrapper.orderByDesc(VerificationProof::getCreatedAt);

        List<ProofVO> list = proofMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(list);
    }

    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        checkAdmin();

        String status = body.get("status");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("status 必须为 APPROVED 或 REJECTED");
        }

        VerificationProof proof = proofMapper.selectById(id);
        if (proof == null) {
            throw new BusinessException(404, "凭证不存在");
        }

        proof.setStatus(status);
        proof.setReviewedBy(CurrentUser.getId());
        proof.setReviewedAt(LocalDateTime.now());
        proofMapper.updateById(proof);

        if ("APPROVED".equals(status)) {
            User user = userMapper.selectById(proof.getUserId());
            if (user != null) {
                user.setVerified(true);
                userMapper.updateById(user);
            }
            notificationService.send(proof.getUserId(), "VERIFY",
                    "认证通过", "恭喜！你的学生认证已通过", proof.getId());
        } else {
            notificationService.send(proof.getUserId(), "VERIFY",
                    "认证未通过", "很抱歉，你的学生认证未通过，请重新提交", proof.getId());
        }

        return Result.success(null);
    }

    /**
     * 封禁用户（逻辑删除）
     */
    @PostMapping("/users/{id}/ban")
    public Result<Void> banUser(@PathVariable Long id) {
        checkAdmin();

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能封禁管理员");
        }

        userMapper.deleteById(id);

        notificationService.send(id, "SYSTEM",
                "账号封禁", "你的账号已被管理员封禁", null);

        return Result.success(null);
    }

    /**
     * 解封用户
     */
    @PostMapping("/users/{id}/unban")
    public Result<Void> unbanUser(@PathVariable Long id) {
        checkAdmin();

        // 恢复逻辑删除的用户
        User user = new User();
        user.setId(id);
        user.setDeleted(0);
        userMapper.updateById(user);

        notificationService.send(id, "SYSTEM",
                "账号解封", "你的账号已解封", null);

        return Result.success(null);
    }

    /**
     * 添加管理员（通过邮箱）
     */
    @PostMapping("/add-admin")
    public Result<Void> addAdmin(@RequestBody Map<String, String> body) {
        checkAdmin();

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("该用户已是管理员");
        }

        user.setRole("ADMIN");
        userMapper.updateById(user);

        notificationService.send(user.getId(), "SYSTEM",
                "管理员权限", "你已被授予管理员权限", null);

        return Result.success(null);
    }

    /**
     * 管理员数据统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        checkAdmin();

        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("verifiedUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getVerified, true)));

        // 商品统计
        stats.put("totalProducts", productMapper.selectCount(null));
        stats.put("activeProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "on")));
        stats.put("soldProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "sold")));

        // 交易统计
        stats.put("totalTrades", tradeMapper.selectCount(null));
        stats.put("pendingTrades", tradeMapper.selectCount(
                new LambdaQueryWrapper<Trade>().eq(Trade::getStatus, "pending")));
        stats.put("completedTrades", tradeMapper.selectCount(
                new LambdaQueryWrapper<Trade>().eq(Trade::getStatus, "completed")));

        // 待审核认证
        stats.put("pendingProofs", proofMapper.selectCount(
                new LambdaQueryWrapper<VerificationProof>().eq(VerificationProof::getStatus, "PENDING")));

        return Result.success(stats);
    }

    private void checkAdmin() {
        Long userId = CurrentUser.getId();
        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    private ProofVO toVO(VerificationProof proof) {
        ProofVO vo = new ProofVO();
        vo.setId(String.valueOf(proof.getId()));
        vo.setUserId(String.valueOf(proof.getUserId()));
        vo.setImageUrl(proof.getImageUrl());
        vo.setSubmitTime(proof.getCreatedAt() != null ? proof.getCreatedAt().toString() : "");
        return vo;
    }
}
