package com.herloop.want;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WantService {

    private final WantMapper wantMapper;
    private final UserMapper userMapper;

    public PageResult<WantVO> listWants(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Want> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Want::getTitle, keyword);
        }
        wrapper.eq(Want::getStatus, "open");
        wrapper.orderByDesc(Want::getCreatedAt);

        Page<Want> p = new Page<>(page, pageSize);
        wantMapper.selectPage(p, wrapper);

        List<WantVO> list = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    public WantVO getById(Long id) {
        Want want = wantMapper.selectById(id);
        if (want == null) {
            throw new BusinessException(404, "求购不存在");
        }
        return toVO(want);
    }

    public WantVO create(Long userId, WantCreateRequest req) {
        Want want = new Want();
        want.setUserId(userId);
        want.setTitle(req.getTitle());
        want.setBudget(req.getBudget());
        want.setDescription(req.getDescription());
        want.setIcon(req.getIcon() != null ? req.getIcon() : "sparkles");
        want.setStatus("open");
        wantMapper.insert(want);
        return toVO(want);
    }

    public void close(Long userId, Long wantId) {
        Want want = wantMapper.selectById(wantId);
        if (want == null) {
            throw new BusinessException(404, "求购不存在");
        }
        if (!want.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人求购");
        }
        want.setStatus("closed");
        wantMapper.updateById(want);
    }

    public PageResult<WantVO> listMyWants(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Want> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Want::getUserId, userId);
        wrapper.orderByDesc(Want::getCreatedAt);

        Page<Want> p = new Page<>(page, pageSize);
        wantMapper.selectPage(p, wrapper);

        List<WantVO> list = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    private WantVO toVO(Want want) {
        WantVO vo = new WantVO();
        vo.setId(want.getId());
        vo.setTitle(want.getTitle());
        vo.setBudget(want.getBudget());
        vo.setDescription(want.getDescription());
        vo.setIcon(want.getIcon());
        vo.setStatus(want.getStatus());
        vo.setCreatedAt(want.getCreatedAt());

        User owner = userMapper.selectById(want.getUserId());
        if (owner != null) {
            WantVO.OwnerBrief brief = new WantVO.OwnerBrief();
            brief.setId(owner.getId());
            brief.setNickname(owner.getNickname());
            brief.setAvatar(owner.getAvatar());
            vo.setOwner(brief);
        }
        return vo;
    }
}
