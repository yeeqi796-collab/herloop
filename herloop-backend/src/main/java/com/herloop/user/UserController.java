package com.herloop.user;

import com.herloop.common.BusinessException;
import com.herloop.common.CurrentUser;
import com.herloop.common.PageResult;
import com.herloop.common.Result;
import com.herloop.points.PointsLogVO;
import com.herloop.points.PointsService;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import com.herloop.product.ProductService;
import com.herloop.product.ProductVO;
import com.herloop.trade.Trade;
import com.herloop.trade.TradeMapper;
import com.herloop.trade.TradeService;
import com.herloop.trade.TradeVO;
import com.herloop.want.WantService;
import com.herloop.want.WantVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final TradeMapper tradeMapper;
    private final ProductService productService;
    private final WantService wantService;
    private final TradeService tradeService;
    private final PointsService pointsService;

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        Long userId = CurrentUser.getId();
        User user = userMapper.selectById(userId);
        return Result.success(toVO(user));
    }

    /**
     * 用户注销（逻辑删除）
     */
    @DeleteMapping("/deactivate")
    public Result<Void> deactivate() {
        Long userId = CurrentUser.getId();
        userMapper.deleteById(userId);
        return Result.success(null);
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = CurrentUser.getId();
        User user = userMapper.selectById(userId);
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("wechat")) user.setWechat(body.get("wechat"));
        userMapper.updateById(user);
        return Result.success(toVO(user));
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = CurrentUser.getId();
        if (file.isEmpty()) {
            throw new BusinessException("请选择图片");
        }
        String ext = file.getOriginalFilename();
        if (ext != null && ext.contains(".")) {
            ext = ext.substring(ext.lastIndexOf("."));
        } else {
            ext = ".jpg";
        }
        String filename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        String baseDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(baseDir, "uploads", "avatars");
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        String url = "/uploads/avatars/" + filename;

        // 更新用户头像
        User user = userMapper.selectById(userId);
        user.setAvatar(url);
        userMapper.updateById(user);

        return Result.success(Map.of("url", url));
    }

    @GetMapping("/products")
    public Result<PageResult<ProductVO>> myProducts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(productService.listMyProducts(userId, status, page, pageSize));
    }

    @GetMapping("/wants")
    public Result<PageResult<WantVO>> myWants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(wantService.listMyWants(userId, page, pageSize));
    }

    @GetMapping("/trades")
    public Result<PageResult<TradeVO>> myTrades(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(tradeService.listMyTrades(userId, type, page, pageSize));
    }

    @GetMapping("/points")
    public Result<Map<String, Integer>> pointsBalance() {
        Long userId = CurrentUser.getId();
        Integer balance = pointsService.getBalance(userId);
        return Result.success(Map.of("points", balance));
    }

    @GetMapping("/points/log")
    public Result<PageResult<PointsLogVO>> pointsLog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = CurrentUser.getId();
        return Result.success(pointsService.listLogs(userId, page, pageSize));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setWechat(user.getWechat());
        vo.setPoints(user.getPoints());
        vo.setVerified(user.getVerified());
        vo.setRole(user.getRole());
        vo.setInviteCode("user_" + user.getId());
        vo.setCreatedAt(user.getCreatedAt());

        Long uid = user.getId();
        Long pCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getUserId, uid));
        vo.setProductCount(pCount.intValue());

        Long tCount = tradeMapper.selectCount(
                new LambdaQueryWrapper<Trade>()
                        .eq(Trade::getBuyerId, uid)
                        .or()
                        .eq(Trade::getSellerId, uid));
        vo.setTradeCount(tCount.intValue());

        return vo;
    }
}
