package com.herloop.points;

import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin() {
        Long userId = CurrentUser.getId();
        int earned = pointsService.checkin(userId);
        return Result.success(Map.of(
                "earned", earned,
                "balance", pointsService.getBalance(userId)
        ));
    }

    @GetMapping("/available")
    public Result<Map<String, Integer>> available() {
        Long userId = CurrentUser.getId();
        return Result.success(Map.of(
                "available", pointsService.getAvailablePoints(userId),
                "total", pointsService.getBalance(userId)
        ));
    }
}
