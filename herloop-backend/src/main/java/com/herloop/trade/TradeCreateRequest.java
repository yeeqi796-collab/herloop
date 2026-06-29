package com.herloop.trade;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TradeCreateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
