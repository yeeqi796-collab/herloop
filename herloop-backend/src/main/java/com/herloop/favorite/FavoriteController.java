package com.herloop.favorite;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import com.herloop.product.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result<Map<String, Boolean>> add(@PathVariable Long productId) {
        Long userId = CurrentUser.getId();
        favoriteService.add(userId, productId);
        return Result.success(Map.of("favorited", true));
    }

    @DeleteMapping("/{productId}")
    public Result<Map<String, Boolean>> remove(@PathVariable Long productId) {
        Long userId = CurrentUser.getId();
        favoriteService.remove(userId, productId);
        return Result.success(Map.of("favorited", false));
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long productId) {
        Long userId = CurrentUser.getId();
        boolean favorited = favoriteService.isFavorited(userId, productId);
        return Result.success(Map.of("favorited", favorited));
    }

    @GetMapping
    public Result<PageResult<ProductVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(favoriteService.listFavorites(userId, page, pageSize));
    }
}
