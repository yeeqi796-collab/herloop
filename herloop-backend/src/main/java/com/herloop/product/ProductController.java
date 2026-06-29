package com.herloop.product;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        productService.delete(userId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        Long userId = CurrentUser.getId();
        productService.updateStatus(userId, id, body.get("status"));
        return Result.success(null);
    }

    @PostMapping("/{id}/images")
    public Result<List<String>> uploadImages(@PathVariable Long id,
                                             @RequestParam("files") MultipartFile[] files) throws IOException {
        Long userId = CurrentUser.getId();

        if (files.length > 5) {
            return Result.error(400, "最多上传5张图片");
        }

        String baseDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(baseDir, "uploads", "products");
        Files.createDirectories(uploadDir);

        List<String> urls = new ArrayList<>();
        StringBuilder urlJson = new StringBuilder();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/products/" + filename;
            urls.add(url);

            if (i > 0) urlJson.append(",");
            urlJson.append("\"").append(url).append("\"");
        }

        productService.addImages(userId, id, urlJson.toString());

        return Result.success(urls);
    }
}
