package com.herloop.product;

import com.herloop.common.BusinessException;
import com.herloop.points.PointsService;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PointsService pointsService;

    @InjectMocks
    private ProductService productService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setNickname("测试用户");
        testUser.setVerified(true);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setUserId(1L);
        testProduct.setTitle("测试商品");
        testProduct.setCategory("服饰");
        testProduct.setConditionDesc("九成新");
        testProduct.setCashPrice(new BigDecimal("50.00"));
        testProduct.setPointsPrice(100);
        testProduct.setTradeMode("cash");
        testProduct.setStatus("on");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(productMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                productService.getById(999L));
    }

    @Test
    void getById_returnsProductVO() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        ProductVO vo = productService.getById(1L);

        assertNotNull(vo);
        assertEquals("测试商品", vo.getTitle());
        assertEquals("服饰", vo.getCategory());
        assertEquals("九成新", vo.getCondition());
    }

    @Test
    void delete_throwsWhenProductReserved() {
        testProduct.setStatus("reserved");
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        assertThrows(BusinessException.class, () ->
                productService.delete(1L, 1L));
    }

    @Test
    void delete_throwsWhenNotOwner() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        assertThrows(BusinessException.class, () ->
                productService.delete(999L, 1L)); // different user
    }

    @Test
    void delete_success() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        productService.delete(1L, 1L);

        verify(productMapper).deleteById(1L);
    }

    @Test
    void updateStatus_throwsWhenNotOwner() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        assertThrows(BusinessException.class, () ->
                productService.updateStatus(999L, 1L, "sold"));
    }

    @Test
    void updateStatus_success() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        productService.updateStatus(1L, 1L, "sold");

        assertEquals("sold", testProduct.getStatus());
        verify(productMapper).updateById(testProduct);
    }
}
