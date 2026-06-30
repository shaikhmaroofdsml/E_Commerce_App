package com.ecommerce.admin.controller;

import com.ecommerce.admin.dto.DashboardStats;
import com.ecommerce.admin.dto.SalesReport;
import com.ecommerce.admin.exception.ForbiddenException;
import com.ecommerce.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Admin REST controller — all endpoints require ROLE_ADMIN (enforced via X-User-Role header
 * set by API Gateway after JWT validation).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin-only: dashboard stats, sales reports, and Excel export. All endpoints require ROLE_ADMIN.")
@SecurityRequirement(name = "Bearer Token")
public class AdminController {

    private final AdminService adminService;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @Operation(summary = "Get dashboard statistics",
               description = "Returns aggregated counts for orders, customers, products, and total revenue.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stats returned",
            content = @Content(schema = @Schema(implementation = DashboardStats.class))),
        @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboard(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ─── Sales Reports ────────────────────────────────────────────────────────

    @Operation(summary = "Sales report for a date range",
               description = "Returns total orders and revenue for the given period.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Report generated",
            content = @Content(schema = @Schema(implementation = SalesReport.class))),
        @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReport> getSalesReport(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Start date (ISO 8601: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (ISO 8601: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(role);
        return ResponseEntity.ok(adminService.getSalesReport(from, to));
    }

    // ─── Excel Export ─────────────────────────────────────────────────────────

    @Operation(summary = "Export orders to Excel (.xlsx)",
               description = "Downloads an Excel file containing all orders in the given date range. " +
                   "Generated using Apache POI.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Excel file returned",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
        @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Start date (yyyy-MM-dd), defaults to 1 month ago")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().minusMonths(1).toString()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd), defaults to today")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        requireAdmin(role);

        try {
            byte[] excelBytes = adminService.exportOrdersToExcel(from, to);
            String filename = "orders_" + from + "_to_" + to + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Excel export failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── Health / ping ────────────────────────────────────────────────────────

    @Operation(summary = "Admin service health ping")
    @ApiResponse(responseCode = "200", description = "Service is running")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("admin-service is running");
    }

    // ─── Guard ───────────────────────────────────────────────────────────────

    private void requireAdmin(String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new ForbiddenException("Admin access required");
        }
    }
}
