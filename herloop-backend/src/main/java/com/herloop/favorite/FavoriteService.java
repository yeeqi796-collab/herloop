package com.herloop.favorite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.PageResult;
import com.herloop.product.Product;
import com.herloop.product.ProductMapper;
import com.herloop.product.ProductVO;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public boolean toggle(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);

        Favorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        }

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        favoriteMapper.insert(fav);
        return true;
    }

    public void add(Long userId, Long productId) {
        if (!isFavorited(userId, productId)) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
        }
    }

    public void remove(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        favoriteMapper.delete(wrapper);
    }

    public boolean isFavorited(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    public PageResult<ProductVO> listFavorites(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> p = new Page<>(page, pageSize);
        favoriteMapper.selectPage(p, wrapper);

        List<ProductVO> list = p.getRecords().stream()
                .map(fav -> {
                    Product product = productMapper.selectById(fav.getProductId());
                    if (product == null) return null;
                    return toProductVO(product);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setCategory(product.getCategory());
        vo.setCondition(product.getConditionDesc());
        vo.setDescription(product.getDescription());
        vo.setCashPrice(product.getCashPrice());
        vo.setPointsPrice(product.getPointsPrice());
        vo.setTradeMode(product.getTradeMode());
        vo.setStatus(product.getStatus());
        vo.setIcon(product.getIcon());
        vo.setCreatedAt(product.getCreatedAt());

        User seller = userMapper.selectById(product.getUserId());
        if (seller != null) {
            ProductVO.SellerBrief brief = new ProductVO.SellerBrief();
            brief.setId(seller.getId());
            brief.setNickname(seller.getNickname());
            brief.setAvatar(seller.getAvatar());
            brief.setVerified(seller.getVerified());
            vo.setSeller(brief);
        }
        return vo;
    }
}
