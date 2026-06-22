package com.herloop.want;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WantCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String budget;

    private String description;

    private String icon;
}
