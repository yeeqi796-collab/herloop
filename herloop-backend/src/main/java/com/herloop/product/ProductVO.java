package com.herloop.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;
    private String title;
    private String category;
    private String condition;
    private String description;
    private BigDecimal cashPrice;
    private Integer pointsPrice;
    private String tradeMode;
    private String status;
    private String icon;
    private String images;
    private LocalDateTime createdAt;
    private SellerBrief seller;

    @Data
    public static class SellerBrief {
        private Long id;
        private String nickname;
        private String avatar;
        private Boolean verified;
    }
}
