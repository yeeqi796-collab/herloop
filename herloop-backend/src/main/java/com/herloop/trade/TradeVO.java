package com.herloop.trade;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeVO {

    private Long id;
    private Long productId;
    private String productTitle;
    private Long buyerId;
    private String buyerNickname;
    private Long sellerId;
    private String sellerNickname;
    private String type;
    private String status;
    private LocalDateTime tradeDate;
    private LocalDateTime createdAt;
}
