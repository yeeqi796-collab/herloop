package com.herloop.trade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.auth.AuthService;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.notification.NotificationService;
import com.herloop.points.PointsLog;
import com.herloop.points.PointsLogMapper;
import com.herloop.points.PointsService;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeMapper tradeMapper;
    private final ProductMapper productMapper;
    private final PointsService pointsService;
    private final PointsLogMapper pointsLogMapper;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Transactional
    public TradeVO create(Long buyerId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!"on".equals(product.getStatus())) {
            throw new BusinessException("该商品当前不可购买");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 积分支付扣减
        int pointsPaid = 0;
        String tradeMode = product.getTradeMode();
        if ("points".equals(tradeMode)) {
            pointsPaid = product.getPointsPrice();
            pointsService.deductPoints(buyerId, pointsPaid, "购买商品: " + product.getTitle(),
                    "PAYMENT", null);
        } else if ("both".equals(tradeMode)) {
            pointsPaid = product.getPointsPrice();
            if (pointsPaid > 0) {
                pointsService.deductPoints(buyerId, pointsPaid, "购买商品(积分部分): " + product.getTitle(),
                        "PAYMENT", null);
            }
        }

        // 创建交易记录
        Trade trade = new Trade();
        trade.setProductId(productId);
        trade.setBuyerId(buyerId);
        trade.setSellerId(product.getUserId());
        trade.setType("buy");
        trade.setStatus("pending");
        trade.setPointsPaid(pointsPaid);
        trade.setTradeDate(LocalDateTime.now());
        tradeMapper.insert(trade);

        // 更新消费记录的 sourceId 为交易ID
        if (pointsPaid > 0) {
            updatePaymentSourceId(buyerId, trade.getId());
        }

        // 商品状态改为 reserved
        product.setStatus("reserved");
        productMapper.updateById(product);

        // 通知卖家有新订单
        notificationService.send(product.getUserId(), "TRADE",
                "新订单", "有人下单了你的商品「" + product.getTitle() + "」", trade.getId());

        return toVO(trade, buyerId);
    }

    @Transactional
    public void complete(Long userId, Long tradeId) {
        Trade trade = tradeMapper.selectById(tradeId);
        if (trade == null) {
            throw new BusinessException(404, "交易不存在");
        }
        if (!"pending".equals(trade.getStatus())) {
            throw new BusinessException("该交易无法确认完成");
        }
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作此交易");
        }

        trade.setStatus("completed");
        tradeMapper.updateById(trade);

        Product product = productMapper.selectById(trade.getProductId());
        if (product != null) {
            product.setStatus("sold");
            productMapper.updateById(product);
        }

        // 积分发放：卖家 +50，买家 +10
        pointsService.changePoints(trade.getSellerId(), 50, "交易完成奖励", "TRADE", tradeId);
        pointsService.changePoints(trade.getBuyerId(), 10, "购买奖励", "TRADE", tradeId);

        // 检查是否是被邀请人的首笔交易，给邀请人发积分
        authService.onInviteeFirstTrade(trade.getBuyerId());

        // 通知双方交易完成
        String productTitle = product != null ? product.getTitle() : "商品";
        notificationService.send(trade.getBuyerId(), "TRADE",
                "交易完成", "你购买的「" + productTitle + "」交易已完成", tradeId);
        notificationService.send(trade.getSellerId(), "TRADE",
                "交易完成", "你出售的「" + productTitle + "」交易已完成", tradeId);
    }

    @Transactional
    public void cancel(Long userId, Long tradeId) {
        Trade trade = tradeMapper.selectById(tradeId);
        if (trade == null) {
            throw new BusinessException(404, "交易不存在");
        }
        if (!"pending".equals(trade.getStatus())) {
            throw new BusinessException("该交易无法取消");
        }
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作此交易");
        }

        trade.setStatus("cancelled");
        tradeMapper.updateById(trade);

        Product product = productMapper.selectById(trade.getProductId());
        if (product != null) {
            product.setStatus("on");
            productMapper.updateById(product);
        }

        // 退还积分
        if (trade.getPointsPaid() != null && trade.getPointsPaid() > 0) {
            pointsService.refundPoints(trade.getBuyerId(), trade.getPointsPaid(),
                    "交易取消退还积分", tradeId);
        }

        // 通知双方交易取消
        Long notifyUserId = userId.equals(trade.getBuyerId()) ? trade.getSellerId() : trade.getBuyerId();
        String productTitle = product != null ? product.getTitle() : "商品";
        notificationService.send(notifyUserId, "TRADE",
                "交易取消", "「" + productTitle + "」的交易已被取消", tradeId);
    }

    public PageResult<TradeVO> listMyTrades(Long userId, String type, int page, int pageSize) {
        LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(type)) {
            if ("buy".equals(type)) {
                wrapper.eq(Trade::getBuyerId, userId);
            } else if ("sell".equals(type)) {
                wrapper.eq(Trade::getSellerId, userId);
            }
        } else {
            wrapper.and(w -> w.eq(Trade::getBuyerId, userId).or().eq(Trade::getSellerId, userId));
        }

        wrapper.orderByDesc(Trade::getTradeDate);

        Page<Trade> p = new Page<>(page, pageSize);
        tradeMapper.selectPage(p, wrapper);

        List<TradeVO> list = p.getRecords().stream()
                .map(t -> toVO(t, userId))
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    private void updatePaymentSourceId(Long userId, Long tradeId) {
        List<PointsLog> logs = pointsLogMapper.selectList(
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .eq(PointsLog::getSourceType, "PAYMENT")
                        .isNull(PointsLog::getSourceId)
                        .orderByDesc(PointsLog::getCreatedAt)
                        .last("LIMIT 1"));
        if (!logs.isEmpty()) {
            PointsLog log = logs.get(0);
            log.setSourceId(tradeId);
            pointsLogMapper.updateById(log);
        }
    }

    private TradeVO toVO(Trade trade, Long currentUserId) {
        TradeVO vo = new TradeVO();
        vo.setId(trade.getId());
        vo.setProductId(trade.getProductId());
        vo.setType(trade.getBuyerId().equals(currentUserId) ? "buy" : "sell");
        vo.setStatus(trade.getStatus());
        vo.setPointsPaid(trade.getPointsPaid());
        vo.setTradeDate(trade.getTradeDate());

        Product product = productMapper.selectById(trade.getProductId());
        if (product != null) {
            TradeVO.ProductBrief brief = new TradeVO.ProductBrief();
            brief.setId(product.getId());
            brief.setTitle(product.getTitle());
            brief.setIcon(product.getIcon());
            brief.setCategory(product.getCategory());
            brief.setTradeMode(product.getTradeMode());
            vo.setProduct(brief);
        }
        return vo;
    }
}
