package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.AnalyticsDto;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.entity.Order;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Export dashboard metrics to CSV format
     */
    public String exportDashboardMetricsToCSV() throws IOException {
        AnalyticsDto.DashboardMetrics metrics = analyticsService.getDashboardMetrics();
        
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("Metric", "Value");
        
        try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            printer.printRecord("Total Revenue", formatCurrency(metrics.getTotalRevenue()));
            printer.printRecord("Completed Auctions", metrics.getCompletedAuctions());
            printer.printRecord("Unpaid Auctions", metrics.getUnpaidAuctions());
            printer.printRecord("Active Auctions", metrics.getActiveAuctions());
            printer.printRecord("Total Bids", metrics.getTotalBids());
            printer.printRecord("Total Auctions", metrics.getTotalAuctions());
            printer.printRecord("Export Date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        
        return sw.toString();
    }
    
    /**
     * Export highest-selling items to CSV
     */
    public String exportHighestSellingItemsToCSV() throws IOException {
        AnalyticsDto.DashboardMetrics metrics = analyticsService.getDashboardMetrics();
        
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader("Rank", "Item Title", "Category", "Winner", "Final Price");
        
        try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            int rank = 1;
            for (AnalyticsDto.TopSellingItem item : metrics.getHighestSellingItems()) {
                printer.printRecord(
                    rank++,
                    item.getTitle(),
                    item.getCategory(),
                    item.getWinnerUsername(),
                    formatCurrency(item.getFinalPrice())
                );
            }
        }
        
        return sw.toString();
    }
    
    /**
     * Export revenue by category to CSV
     */
    public String exportRevenueByCategoryToCSV() throws IOException {
        AnalyticsDto.DashboardMetrics metrics = analyticsService.getDashboardMetrics();
        
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader("Category", "Revenue", "Auction Count", "Average Price");
        
        try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            for (AnalyticsDto.CategoryRevenue cat : metrics.getRevenueByCategory()) {
                BigDecimal avgPrice = cat.getAuctionCount() > 0 
                    ? cat.getRevenue().divide(BigDecimal.valueOf(cat.getAuctionCount()), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                    
                printer.printRecord(
                    cat.getCategory(),
                    formatCurrency(cat.getRevenue()),
                    cat.getAuctionCount(),
                    formatCurrency(avgPrice)
                );
            }
        }
        
        return sw.toString();
    }
    
    /**
     * Export complete metrics report to CSV (all sections)
     */
    public String exportCompleteMetricsReportToCSV() throws IOException {
        StringBuilder report = new StringBuilder();
        
        // Metrics summary
        report.append("DASHBOARD METRICS REPORT\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).append("\n\n");
        
        report.append("=== KEY METRICS ===\n");
        report.append(exportDashboardMetricsToCSV());
        
        report.append("\n=== HIGHEST-SELLING ITEMS ===\n");
        report.append(exportHighestSellingItemsToCSV());
        
        report.append("\n=== REVENUE BY CATEGORY ===\n");
        report.append(exportRevenueByCategoryToCSV());
        
        return report.toString();
    }
    
    /**
     * Generate HTML format for PDF conversion (via external tool)
     */
    public String generateMetricsHTML() {
        AnalyticsDto.DashboardMetrics metrics = analyticsService.getDashboardMetrics();
        LocalDateTime exportTime = LocalDateTime.now();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Analytics Dashboard Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append("h1 { color: #333; border-bottom: 3px solid #6366f1; padding-bottom: 10px; }\n");
        html.append("h2 { color: #555; margin-top: 30px; }\n");
        html.append(".metrics-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin: 20px 0; }\n");
        html.append(".metric-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-left: 4px solid #6366f1; }\n");
        html.append(".metric-value { font-size: 28px; font-weight: bold; color: #6366f1; }\n");
        html.append(".metric-label { font-size: 12px; color: #999; text-transform: uppercase; margin-top: 5px; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; background: white; }\n");
        html.append("th { background-color: #6366f1; color: white; padding: 12px; text-align: left; }\n");
        html.append("td { padding: 12px; border-bottom: 1px solid #eee; }\n");
        html.append("tr:hover { background-color: #f9f9f9; }\n");
        html.append(".footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #999; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("<h1>📊 Analytics Dashboard Report</h1>\n");
        html.append("<p>Generated on: ").append(exportTime).append("</p>\n");
        
        // Key Metrics
        html.append("<h2>Key Metrics</h2>\n");
        html.append("<div class=\"metrics-grid\">\n");
        html.append(generateMetricCard("💰 Total Revenue", formatCurrency(metrics.getTotalRevenue()), "#22c55e"));
        html.append(generateMetricCard("✅ Completed Auctions", String.valueOf(metrics.getCompletedAuctions()), "#818cf8"));
        html.append(generateMetricCard("⏳ Unpaid Auctions", String.valueOf(metrics.getUnpaidAuctions()), "#f59e0b"));
        html.append(generateMetricCard("🔥 Active Auctions", String.valueOf(metrics.getActiveAuctions()), "#ef4444"));
        html.append(generateMetricCard("🔨 Total Bids", String.valueOf(metrics.getTotalBids()), "#3b82f6"));
        html.append(generateMetricCard("📦 Total Auctions", String.valueOf(metrics.getTotalAuctions()), "#6b7280"));
        html.append("</div>\n");
        
        // Highest-Selling Items
        html.append("<h2>Highest-Selling Items</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Rank</th><th>Item Title</th><th>Category</th><th>Winner</th><th>Final Price</th></tr>\n");
        int rank = 1;
        for (AnalyticsDto.TopSellingItem item : metrics.getHighestSellingItems()) {
            html.append("<tr>\n");
            html.append("<td>#").append(rank++).append("</td>\n");
            html.append("<td>").append(item.getTitle()).append("</td>\n");
            html.append("<td>").append(item.getCategory()).append("</td>\n");
            html.append("<td>").append(item.getWinnerUsername()).append("</td>\n");
            html.append("<td>").append(formatCurrency(item.getFinalPrice())).append("</td>\n");
            html.append("</tr>\n");
        }
        html.append("</table>\n");
        
        // Revenue by Category
        html.append("<h2>Revenue by Category</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Category</th><th>Revenue</th><th>Auctions</th><th>Average</th></tr>\n");
        for (AnalyticsDto.CategoryRevenue cat : metrics.getRevenueByCategory()) {
            BigDecimal avgPrice = cat.getAuctionCount() > 0 
                ? cat.getRevenue().divide(BigDecimal.valueOf(cat.getAuctionCount()), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
                
            html.append("<tr>\n");
            html.append("<td>").append(cat.getCategory()).append("</td>\n");
            html.append("<td>").append(formatCurrency(cat.getRevenue())).append("</td>\n");
            html.append("<td>").append(cat.getAuctionCount()).append("</td>\n");
            html.append("<td>").append(formatCurrency(avgPrice)).append("</td>\n");
            html.append("</tr>\n");
        }
        html.append("</table>\n");
        
        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("<p>This report was automatically generated by the Bidoo Analytics Dashboard.</p>\n");
        html.append("</div>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private String generateMetricCard(String label, String value, String color) {
        return String.format(
            "<div class=\"metric-card\" style=\"border-left-color: %s;\">\n" +
            "  <div class=\"metric-label\">%s</div>\n" +
            "  <div class=\"metric-value\" style=\"color: %s;\">%s</div>\n" +
            "</div>\n",
            color, label, color, value
        );
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }
}
