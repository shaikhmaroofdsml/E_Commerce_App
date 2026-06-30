package com.ecommerce.product.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private boolean active;
}
