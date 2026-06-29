package com.herloop.points;

import com.herloop.common.BusinessException;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private PointsLogMapper pointsLogMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DailyCheckinMapper checkinMapper;

    @InjectMocks
    private PointsService pointsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setNickname("测试用户");
        testUser.setPoints(100);
    }

    @Test
    void getBalance_returnsUserPoints() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        Integer balance = pointsService.getBalance(1L);

        assertEquals(100, balance);
    }

    @Test
    void getBalance_returnsZeroWhenUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        Integer balance = pointsService.getBalance(999L);

        assertEquals(0, balance);
    }

    @Test
    void changePoints_addsPointsAndCreatesLog() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        pointsService.changePoints(1L, 50, "测试积分", "TEST", null);

        assertEquals(150, testUser.getPoints());
        verify(userMapper).updateById(any(User.class));
        verify(pointsLogMapper).insert(any(PointsLog.class));
    }

    @Test
    void changePoints_doesNotExceedCap() {
        testUser.setPoints(4980);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        pointsService.changePoints(1L, 100, "测试积分", "TEST", null);

        // 4980 + 20 = 5000 (cap), not 4980 + 100
        assertEquals(5000, testUser.getPoints());
    }

    @Test
    void deductPoints_throwsWhenInsufficient() {
        testUser.setPoints(10);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(pointsLogMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        assertThrows(BusinessException.class, () ->
                pointsService.deductPoints(1L, 50, "购买", "PAYMENT", null));
    }

    @Test
    void checkin_throwsWhenAlreadyCheckedIn() {
        when(checkinMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () ->
                pointsService.checkin(1L));
    }

    @Test
    void checkin_success() {
        when(checkinMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        int earned = pointsService.checkin(1L);

        assertEquals(5, earned);
        verify(checkinMapper).insert(any(DailyCheckin.class));
    }
}
