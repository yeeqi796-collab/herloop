package com.herloop.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.PageResult;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsLogMapper pointsLogMapper;
    private final UserMapper userMapper;

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

    public Integer getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getPoints() : 0;
    }

    @Transactional
    public void changePoints(Long userId, int amount, String description) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setPoints(user.getPoints() + amount);
        userMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setDescription(description);
        pointsLogMapper.insert(log);
    }

    private PointsLogVO toVO(PointsLog log) {
        PointsLogVO vo = new PointsLogVO();
        vo.setId(log.getId());
        vo.setAmount(log.getAmount());
        vo.setDescription(log.getDescription());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
