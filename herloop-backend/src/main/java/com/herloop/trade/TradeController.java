package com.herloop.trade;

import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public Result<TradeVO> create(@Valid @RequestBody TradeCreateRequest req) {
        Long userId = CurrentUser.getId();
        return Result.success(tradeService.create(userId, req.getProductId()));
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        tradeService.complete(userId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        tradeService.cancel(userId, id);
        return Result.success(null);
    }
}
