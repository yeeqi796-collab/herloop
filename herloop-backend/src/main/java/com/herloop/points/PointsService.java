package com.herloop.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsService {

    private static final int MAX_POINTS = 5000;
    private static final int CHECKIN_POINTS = 5;
    private static final int PUBLISH_POINTS = 20;
    private static final int TRADE_REWARD_POINTS = 50;
    private static final int INVITE_POINTS = 100;
    private static final int REPORT_PENALTY_POINTS = -30;

    private final PointsLogMapper pointsLogMapper;
    private final UserMapper userMapper;
    private final DailyCheckinMapper checkinMapper;

    // ========== 积分查询 ==========

    public Integer getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getPoints() : 0;
    }

    /**
     * 计算可用积分（排除已过期和已使用的）
     */
    public int getAvailablePoints(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        // 查询未过期且未用完的积分总额
        List<PointsLog> logs = pointsLogMapper.selectList(
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .gt(PointsLog::getExpireAt, now));

        int total = 0;
        for (PointsLog log : logs) {
            total += log.getAmount() - log.getUsed();
        }
        return Math.max(0, total);
    }

    public PageResult<PointsLogVO> listLogs(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId);
        wrapper.orderByDesc(PointsLog::getCreatedAt);

        Page<PointsLog> p = new Page<>(page, pageSize);
        pointsLogMapper.selectPage(p, wrapper);

        List<PointsLogVO> list = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    // ========== 积分变更 ==========

    /**
     * 增加积分（通用方法，带来源类型）
     */
    @Transactional
    public void changePoints(Long userId, int amount, String description,
                             String sourceType, Long sourceId) {
        if (amount == 0) return;

        User user = userMapper.selectById(userId);
        if (user == null) return;

        // 检查 5000 上限（仅正向增加时）
        if (amount > 0) {
            int currentPoints = user.getPoints();
            if (currentPoints + amount > MAX_POINTS) {
                amount = MAX_POINTS - currentPoints;
                if (amount <= 0) return;
            }
        }

        user.setPoints(user.getPoints() + amount);
        userMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setDescription(description);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setExpireAt(LocalDateTime.now().plusMonths(12));
        log.setUsed(0);
        pointsLogMapper.insert(log);
    }

    /**
     * 简化版（兼容旧调用）
     */
    @Transactional
    public void changePoints(Long userId, int amount, String description) {
        changePoints(userId, amount, description, null, null);
    }

    /**
     * 消费积分（FIFO，优先消耗最早过期的）
     */
    @Transactional
    public void deductPoints(Long userId, int amount, String description,
                             String sourceType, Long sourceId) {
        if (amount <= 0) return;

        int available = getAvailablePoints(userId);
        if (available < amount) {
            throw new BusinessException("积分不足，当前可用: " + available);
        }

        LocalDateTime now = LocalDateTime.now();
        // 查询未过期的积分记录，按过期时间升序（FIFO）
        List<PointsLog> logs = pointsLogMapper.selectList(
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .gt(PointsLog::getExpireAt, now)
                        .orderByAsc(PointsLog::getExpireAt));

        int remaining = amount;
        for (PointsLog log : logs) {
            if (remaining <= 0) break;
            int availableInLog = log.getAmount() - log.getUsed();
            if (availableInLog <= 0) continue;

            int deduct = Math.min(remaining, availableInLog);
            log.setUsed(log.getUsed() + deduct);
            pointsLogMapper.updateById(log);
            remaining -= deduct;
        }

        // 记录消费流水
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() - amount);
            userMapper.updateById(user);
        }

        PointsLog deductLog = new PointsLog();
        deductLog.setUserId(userId);
        deductLog.setAmount(-amount);
        deductLog.setDescription(description);
        deductLog.setSourceType(sourceType);
        deductLog.setSourceId(sourceId);
        deductLog.setExpireAt(now.plusMonths(12));
        deductLog.setUsed(amount); // 消费记录本身标记为已使用
        pointsLogMapper.insert(deductLog);
    }

    /**
     * 退还积分（恢复对应记录的 used）
     */
    @Transactional
    public void refundPoints(Long userId, int amount, String description, Long tradeId) {
        if (amount <= 0) return;

        // 查找该交易的消费记录
        List<PointsLog> logs = pointsLogMapper.selectList(
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .eq(PointsLog::getSourceType, "PAYMENT")
                        .eq(PointsLog::getSourceId, tradeId));

        int remaining = amount;
        for (PointsLog log : logs) {
            if (remaining <= 0) break;
            if (log.getUsed() <= 0) continue;

            int restore = Math.min(remaining, log.getUsed());
            log.setUsed(log.getUsed() - restore);
            pointsLogMapper.updateById(log);
            remaining -= restore;
        }

        // 恢复用户余额
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + amount);
            userMapper.updateById(user);
        }

        // 记录退还流水
        PointsLog refundLog = new PointsLog();
        refundLog.setUserId(userId);
        refundLog.setAmount(amount);
        refundLog.setDescription(description);
        refundLog.setSourceType("REFUND");
        refundLog.setSourceId(tradeId);
        refundLog.setExpireAt(LocalDateTime.now().plusMonths(12));
        refundLog.setUsed(0);
        pointsLogMapper.insert(refundLog);
    }

    // ========== 签到 ==========

    @Transactional
    public int checkin(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        Long count = checkinMapper.selectCount(
                new LambdaQueryWrapper<DailyCheckin>()
                        .eq(DailyCheckin::getUserId, userId)
                        .eq(DailyCheckin::getCheckinDate, today));
        if (count > 0) {
            throw new BusinessException("今日已签到");
        }

        // 记录签到
        DailyCheckin checkin = new DailyCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkinMapper.insert(checkin);

        // 发放积分
        changePoints(userId, CHECKIN_POINTS, "每日签到", "CHECKIN", null);

        return CHECKIN_POINTS;
    }

    // ========== VO 转换 ==========

    private PointsLogVO toVO(PointsLog log) {
        PointsLogVO vo = new PointsLogVO();
        vo.setId(log.getId());
        int amt = log.getAmount();
        vo.setAmount(amt >= 0 ? "+" + amt : String.valueOf(amt));
        vo.setDescription(log.getDescription());
        vo.setSourceType(log.getSourceType());
        vo.setExpireAt(log.getExpireAt());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
