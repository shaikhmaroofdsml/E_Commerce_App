package com.ecommerce.admin.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStats {
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue;
    private long totalCustomers;
    private long totalProducts;
    private long outOfStockProducts;
}
