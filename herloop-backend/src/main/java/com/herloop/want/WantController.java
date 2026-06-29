package com.herloop.want;

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
import java.util.UUID;

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

    @PostMapping("/{id}/images")
    public Result<List<String>> uploadImages(@PathVariable Long id,
                                             @RequestParam("files") MultipartFile[] files) throws IOException {
        Long userId = CurrentUser.getId();

        if (files.length > 5) {
            return Result.error(400, "最多上传5张图片");
        }

        String baseDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(baseDir, "uploads", "wants");
        Files.createDirectories(uploadDir);

        List<String> urls = new ArrayList<>();
        StringBuilder urlJson = new StringBuilder();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/wants/" + filename;
            urls.add(url);

            if (i > 0) urlJson.append(",");
            urlJson.append("\"").append(url).append("\"");
        }

        wantService.addImages(userId, id, urlJson.toString());

        return Result.success(urls);
    }

}
