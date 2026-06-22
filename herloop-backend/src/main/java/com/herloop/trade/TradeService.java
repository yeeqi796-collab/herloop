package com.herloop.trade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.PageResult;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeMapper tradeMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public PageResult<TradeVO> listMyTrades(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trade::getBuyerId, userId)
                .or()
                .eq(Trade::getSellerId, userId);
        wrapper.orderByDesc(Trade::getCreatedAt);

        Page<Trade> p = new Page<>(page, pageSize);
        tradeMapper.selectPage(p, wrapper);

        List<TradeVO> list = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    private TradeVO toVO(Trade trade) {
        TradeVO vo = new TradeVO();
        vo.setId(trade.getId());
        vo.setProductId(trade.getProductId());
        vo.setBuyerId(trade.getBuyerId());
        vo.setSellerId(trade.getSellerId());
        vo.setType(trade.getType());
        vo.setStatus(trade.getStatus());
        vo.setTradeDate(trade.getTradeDate());
        vo.setCreatedAt(trade.getCreatedAt());

        Product product = productMapper.selectById(trade.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
        }
        User buyer = userMapper.selectById(trade.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        User seller = userMapper.selectById(trade.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        return vo;
    }
}
