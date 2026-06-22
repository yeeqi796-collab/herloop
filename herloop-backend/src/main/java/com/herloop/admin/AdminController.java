package com.herloop.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.herloop.auth.VerificationProof;
import com.herloop.auth.VerificationProofMapper;
import com.herloop.common.BusinessException;
import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VerificationProofMapper proofMapper;
    private final UserMapper userMapper;

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
        }

        return Result.success(null);
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
