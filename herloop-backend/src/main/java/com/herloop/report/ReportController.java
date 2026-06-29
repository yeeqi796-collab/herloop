package com.herloop.report;

import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<Report> create(@Valid @RequestBody ReportCreateRequest req) {
        Long userId = CurrentUser.getId();
        return Result.success(reportService.create(userId, req));
    }

    @GetMapping("/pending")
    public Result<PageResult<Report>> listPending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.listPending(page, pageSize));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam String action) {
        Long adminId = CurrentUser.getId();
        reportService.audit(adminId, id, action);
        return Result.success(null);
    }
}
