package com.herloop.auth;

import com.herloop.common.CurrentUser;
import com.herloop.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationProofMapper proofMapper;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse response = authService.register(req);
        return Result.success("注册成功", response);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse response = authService.login(req);
        return Result.success(response);
    }

    @PostMapping("/submit-proof")
    public Result<Map<String, Long>> submitProof(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = CurrentUser.getId();

        String baseDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(baseDir, "uploads", "proofs");
        Files.createDirectories(uploadDir);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        VerificationProof proof = new VerificationProof();
        proof.setUserId(userId);
        proof.setImageUrl("/uploads/proofs/" + filename);
        proof.setStatus("PENDING");
        proofMapper.insert(proof);

        return Result.success(Map.of("id", proof.getId()));
    }
}
