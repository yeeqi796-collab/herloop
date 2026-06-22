package com.herloop.user;

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

import java.util.Map;

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
