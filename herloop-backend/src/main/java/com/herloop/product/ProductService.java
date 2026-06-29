package com.herloop.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herloop.common.BusinessException;
import com.herloop.common.PageResult;
import com.herloop.points.PointsService;
import com.herloop.user.User;
import com.herloop.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;

    public PageResult<ProductVO> listProducts(ProductQueryRequest req) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(req.getCategory()) && !"全部".equals(req.getCategory())) {
            wrapper.eq(Product::getCategory, req.getCategory());
        }
        if (StringUtils.hasText(req.getTradeMode())) {
            wrapper.eq(Product::getTradeMode, req.getTradeMode());
        }
        if (StringUtils.hasText(req.getStatus())) {
            wrapper.eq(Product::getStatus, req.getStatus());
        }

        // 搜索优化：使用 FULLTEXT 索引
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.apply("MATCH(title, description) AGAINST({0} IN BOOLEAN MODE)", req.getKeyword());
        }

        // 游标分页
        if (req.getCursor() != null) {
            wrapper.lt(Product::getId, req.getCursor());
            wrapper.orderByDesc(Product::getId);
            // 游标分页不需要 offset，直接查 pageSize 条
            List<Product> records = productMapper.selectList(wrapper.last("LIMIT " + req.getPageSize()));
            List<ProductVO> list = records.stream().map(this::toVO).collect(Collectors.toList());
            return new PageResult<>(list, list.size(), 1, req.getPageSize());
        }

        // 传统 offset 分页
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> page = new Page<>(req.getPage(), req.getPageSize());
        productMapper.selectPage(page, wrapper);

        List<ProductVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, page.getTotal(), req.getPage(), req.getPageSize());
    }

    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return toVO(product);
    }

    public ProductVO create(Long userId, ProductCreateRequest req) {
        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(req.getTitle());
        product.setCategory(req.getCategory());
        product.setConditionDesc(req.getCondition());
        product.setDescription(req.getDescription());
        product.setCashPrice(req.getCashPrice());
        product.setPointsPrice(req.getPointsPrice() != null ? req.getPointsPrice() : 0);
        product.setTradeMode(req.getTradeMode());
        product.setStatus("on");
        product.setIcon(req.getIcon() != null ? req.getIcon() : "bag");
        product.setImages(req.getImages());
        product.setWechat(req.getWechat());
        productMapper.insert(product);

        // 首次发布商品送积分
        Long publishCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getUserId, userId));
        if (publishCount == 1) {
            pointsService.changePoints(userId, 20, "首次发布商品奖励", "PUBLISH", product.getId());
        }

        return toVO(product);
    }

    public void delete(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人商品");
        }
        if ("reserved".equals(product.getStatus())) {
            throw new BusinessException("商品正在交易中，无法删除");
        }
        productMapper.deleteById(productId);
    }

    public void updateStatus(Long userId, Long productId, String status) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人商品");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    public void addImages(Long userId, Long productId, String imageUrls) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人商品");
        }
        // 追加图片到现有列表
        String existing = product.getImages();
        if (StringUtils.hasText(existing) && !"[]".equals(existing)) {
            // 拼接 JSON 数组：去掉末尾 ]，加上新 URL 和 ]
            existing = existing.substring(0, existing.length() - 1) + "," + imageUrls + "]";
        } else {
            existing = "[" + imageUrls + "]";
        }
        product.setImages(existing);
        productMapper.updateById(product);
    }

    public PageResult<ProductVO> listMyProducts(Long userId, String status, int page, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        Page<Product> p = new Page<>(page, pageSize);
        productMapper.selectPage(p, wrapper);

        List<ProductVO> list = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, p.getTotal(), page, pageSize);
    }

    private ProductVO toVO(Product product) {
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
        vo.setImages(product.getImages());
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
