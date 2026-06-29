package com.herloop.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.points.PointsService;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final PointsService pointsService;
    private final ProductMapper productMapper;

    public Report create(Long reporterId, ReportCreateRequest req) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(req.getTargetType());
        report.setTargetId(req.getTargetId());
        report.setReason(req.getReason());
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return report;
    }

    public PageResult<Report> listPending(int page, int pageSize) {
        Page<Report> p = new Page<>(page, pageSize);
        reportMapper.selectPage(p,
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")
                        .orderByDesc(Report::getCreatedAt));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public void audit(Long adminId, Long reportId, String action) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "举报不存在");
        }
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("该举报已处理");
        }

        report.setReviewedBy(adminId);
        report.setReviewedAt(LocalDateTime.now());

        if ("CONFIRMED".equals(action)) {
            report.setStatus("CONFIRMED");
            Long penalizedUserId = resolvePenalizedUserId(report);
            if (penalizedUserId != null) {
                pointsService.changePoints(penalizedUserId, -30, "举报成立扣分",
                        "REPORT", report.getId());
            }
        } else {
            report.setStatus("REJECTED");
        }

        reportMapper.updateById(report);
    }

    private Long resolvePenalizedUserId(Report report) {
        if ("USER".equals(report.getTargetType())) {
            return report.getTargetId();
        }
        if ("PRODUCT".equals(report.getTargetType())) {
            Product product = productMapper.selectById(report.getTargetId());
            return product != null ? product.getUserId() : null;
        }
        return null;
    }
}
