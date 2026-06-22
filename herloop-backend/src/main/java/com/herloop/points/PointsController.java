package com.herloop.points;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/balance")
    public Result<Map<String, Integer>> balance() {
        Long userId = CurrentUser.getId();
        Integer balance = pointsService.getBalance(userId);
        return Result.success(Map.of("balance", balance));
    }

    @GetMapping("/logs")
    public Result<PageResult<PointsLogVO>> logs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(pointsService.listLogs(userId, page, pageSize));
    }
}
