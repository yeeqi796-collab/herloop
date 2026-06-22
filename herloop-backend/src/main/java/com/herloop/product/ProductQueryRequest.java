package com.herloop.product;

import lombok.Data;

@Data
public class ProductQueryRequest {

    private String category;
    private String tradeMode;
    private String status;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
