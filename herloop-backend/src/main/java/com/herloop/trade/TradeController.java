package com.herloop.trade;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/mine")
    public Result<PageResult<TradeVO>> myTrades(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(tradeService.listMyTrades(userId, page, pageSize));
    }
}
