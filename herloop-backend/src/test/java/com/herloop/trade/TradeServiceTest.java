package com.herloop.trade;

import com.herloop.auth.AuthService;
import com.herloop.common.BusinessException;
import com.herloop.notification.NotificationService;
import com.herloop.points.PointsLogMapper;
import com.herloop.points.PointsService;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeMapper tradeMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PointsService pointsService;

    @Mock
    private PointsLogMapper pointsLogMapper;

    @Mock
    private AuthService authService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TradeService tradeService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setUserId(2L); // seller
        testProduct.setTitle("测试商品");
        testProduct.setCategory("服饰");
        testProduct.setConditionDesc("九成新");
        testProduct.setCashPrice(new BigDecimal("50.00"));
        testProduct.setPointsPrice(100);
        testProduct.setTradeMode("cash");
        testProduct.setStatus("on");
    }

    @Test
    void create_throwsWhenProductNotFound() {
        when(productMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                tradeService.create(1L, 999L));
    }

    @Test
    void create_throwsWhenProductNotOn() {
        testProduct.setStatus("sold");
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        assertThrows(BusinessException.class, () ->
                tradeService.create(1L, 1L));
    }

    @Test
    void create_throwsWhenBuyerIsSeller() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        assertThrows(BusinessException.class, () ->
                tradeService.create(2L, 1L)); // buyerId = sellerId
    }

    @Test
    void create_successForCashMode() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        TradeVO result = tradeService.create(1L, 1L);

        assertNotNull(result);
        assertEquals("buy", result.getType());
        assertEquals("pending", result.getStatus());
        verify(productMapper).updateById(any(Product.class));
        verify(notificationService).send(eq(2L), eq("TRADE"), any(), any(), any());
    }

    @Test
    void complete_throwsWhenNotPending() {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setStatus("completed");
        when(tradeMapper.selectById(1L)).thenReturn(trade);

        assertThrows(BusinessException.class, () ->
                tradeService.complete(1L, 1L));
    }

    @Test
    void cancel_refundsPoints() {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setBuyerId(1L);
        trade.setSellerId(2L);
        trade.setProductId(1L);
        trade.setStatus("pending");
        trade.setPointsPaid(100);

        when(tradeMapper.selectById(1L)).thenReturn(trade);
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        tradeService.cancel(1L, 1L);

        verify(pointsService).refundPoints(eq(1L), eq(100), any(), eq(1L));
        verify(productMapper).updateById(any(Product.class));
    }
}
