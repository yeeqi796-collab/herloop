package com.herloop.product;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> list(ProductQueryRequest req) {
        return Result.success(productService.listProducts(req));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductCreateRequest req) {
        Long userId = CurrentUser.getId();
        return Result.success(productService.create(userId, req));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        Long userId = CurrentUser.getId();
        productService.updateStatus(userId, id, status);
        return Result.success(null);
    }

    @GetMapping("/mine")
    public Result<PageResult<ProductVO>> mine(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(productService.listMyProducts(userId, status, page, pageSize));
    }
}
