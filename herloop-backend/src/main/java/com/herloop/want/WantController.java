package com.herloop.want;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wants")
@RequiredArgsConstructor
public class WantController {

    private final WantService wantService;

    @GetMapping
    public Result<PageResult<WantVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wantService.listWants(keyword, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<WantVO> detail(@PathVariable Long id) {
        return Result.success(wantService.getById(id));
    }

    @PostMapping
    public Result<WantVO> create(@Valid @RequestBody WantCreateRequest req) {
        Long userId = CurrentUser.getId();
        return Result.success(wantService.create(userId, req));
    }

    @PatchMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        wantService.close(userId, id);
        return Result.success(null);
    }

}
