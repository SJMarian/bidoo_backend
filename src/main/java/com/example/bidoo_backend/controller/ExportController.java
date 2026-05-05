package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {
    
    private final ExportService exportService;
    
    /**
     * Export dashboard metrics as CSV
     */
    @GetMapping("/metrics/csv")
    public ResponseEntity<String> exportMetricsCSV() throws IOException {
        String csv = exportService.exportDashboardMetricsToCSV();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"metrics_" + timestamp + ".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
    
    /**
     * Export highest-selling items as CSV
     */
    @GetMapping("/items/csv")
    public ResponseEntity<String> exportItemsCSV() throws IOException {
        String csv = exportService.exportHighestSellingItemsToCSV();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"highest_items_" + timestamp + ".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
    
    /**
     * Export revenue by category as CSV
     */
    @GetMapping("/category/csv")
    public ResponseEntity<String> exportCategoryCSV() throws IOException {
        String csv = exportService.exportRevenueByCategoryToCSV();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"revenue_by_category_" + timestamp + ".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
    
    /**
     * Export complete report as CSV
     */
    @GetMapping("/report/csv")
    public ResponseEntity<String> exportCompleteReportCSV() throws IOException {
        String csv = exportService.exportCompleteMetricsReportToCSV();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"analytics_report_" + timestamp + ".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
    
    /**
     * Export metrics as HTML (for PDF conversion or web view)
     */
    @GetMapping("/report/html")
    public ResponseEntity<String> exportReportHTML() {
        String html = exportService.generateMetricsHTML();
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
    
    /**
     * Export metrics as JSON for custom processing
     */
    @GetMapping("/metrics/json")
    public ResponseEntity<String> exportMetricsJSON() {
        // Can be used for data processing or third-party integrations
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"JSON export - use /api/admin/analytics/dashboard for metrics\"}");
    }
}
