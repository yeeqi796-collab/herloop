package com.herloop.trade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.PageResult;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeMapper tradeMapper;
    private final ProductMapper productMapper;

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

    private TradeVO toVO(Trade trade, Long currentUserId) {
        TradeVO vo = new TradeVO();
        vo.setId(trade.getId());
        vo.setProductId(trade.getProductId());
        vo.setType(trade.getBuyerId().equals(currentUserId) ? "buy" : "sell");
        vo.setStatus(trade.getStatus());
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
