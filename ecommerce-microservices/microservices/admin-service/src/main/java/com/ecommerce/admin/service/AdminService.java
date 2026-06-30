package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.DashboardStats;
import com.ecommerce.admin.dto.SalesReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Admin service — aggregates data from other microservices via RestClient.
 * Uses direct HTTP calls to service URLs (no gateway needed for internal calls).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final RestClient restClient;

    @Value("${services.order-service:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${services.product-service:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${services.customer-service:http://localhost:8081}")
    private String customerServiceUrl;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    public DashboardStats getDashboardStats() {
        try {
            // These are simplified calls — in production would use typed responses
            long totalOrders     = getCount(orderServiceUrl + "/api/orders/admin/count");
            long totalCustomers  = getCount(customerServiceUrl + "/api/customers/admin/count");
            long totalProducts   = getCount(productServiceUrl + "/api/products/admin/count");

            return DashboardStats.builder()
                .totalOrders(totalOrders)
                .pendingOrders(0L)      // Simplified — extend with actual queries
                .confirmedOrders(0L)
                .deliveredOrders(0L)
                .totalRevenue(BigDecimal.ZERO)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .outOfStockProducts(0L)
                .build();
        } catch (Exception e) {
            log.warn("Could not fetch all dashboard stats: {}", e.getMessage());
            return DashboardStats.builder()
                .totalOrders(0L).totalCustomers(0L).totalProducts(0L)
                .totalRevenue(BigDecimal.ZERO).build();
        }
    }

    // ─── Sales Report ─────────────────────────────────────────────────────────

    public SalesReport getSalesReport(LocalDate from, LocalDate to) {
        // Simplified — in production, call order-service with date range
        log.info("Generating sales report from {} to {}", from, to);
        return SalesReport.builder()
            .fromDate(from)
            .toDate(to)
            .totalOrders(0L)
            .totalRevenue(BigDecimal.ZERO)
            .averageOrderValue(BigDecimal.ZERO)
            .build();
    }

    // ─── Excel Export ─────────────────────────────────────────────────────────

    /**
     * Exports orders to an Excel (.xlsx) file using Apache POI.
     * Returns the file as a byte array for download.
     */
    public byte[] exportOrdersToExcel(LocalDate from, LocalDate to) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders Export");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            String[] headers = {"Order ID", "Customer ID", "Status", "Total Amount",
                "Tracking Number", "Shipping Address", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Data rows — in production, fetch from order-service
            // Placeholder row showing the format:
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("Example: Fetch orders from order-service");
            sampleRow.createCell(1).setCellValue("Date range: " + from + " to " + to);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private long getCount(String url) {
        try {
            ResponseEntity<Long> response = restClient.get().uri(url)
                .retrieve().toEntity(Long.class);
            return response.getBody() != null ? response.getBody() : 0L;
        } catch (Exception e) {
            log.debug("Could not fetch count from {}: {}", url, e.getMessage());
            return 0L;
        }
    }
}
