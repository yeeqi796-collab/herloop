package com.herloop.trade;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeVO {

    private Long id;
    private Long productId;
    private ProductBrief product;
    private String type;
    private String status;
    private Integer pointsPaid;
    private LocalDateTime tradeDate;

    @Data
    public static class ProductBrief {
        private Long id;
        private String title;
        private String icon;
        private String category;
        private String tradeMode;
    }
}
