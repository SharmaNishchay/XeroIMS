package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.DashboardDTO;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.model.Sale;
import com.darkhex.xeroims.model.Purchase;
import com.darkhex.xeroims.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    public DashboardDTO getDashboardData(User user, OauthUser oauthUser) {
        DashboardDTO dto = new DashboardDTO();
        // Metrics
        dto.setTotalProducts((user != null) ? productRepository.findAllByUserOrderByNameAsc(user).size() :
                (oauthUser != null) ? productRepository.findAllByOauthUserOrderByNameAsc(oauthUser).size() : 0);
        dto.setTotalCategories((user != null) ? categoryRepository.findAllByUserOrderByNameAsc(user).size() :
                (oauthUser != null) ? categoryRepository.findAllByOauthUserOrderByNameAsc(oauthUser).size() : 0);
        dto.setTotalSuppliers((user != null) ? supplierRepository.findAllByUserOrderByNameAsc(user).size() :
                (oauthUser != null) ? supplierRepository.findAllByOauthUserOrderByNameAsc(oauthUser).size() : 0);
        // Only count completed sales and purchases
        List<Sale> allSales = (user != null) ? saleRepository.findAllByUserOrderBySalesDateDesc(user) :
                (oauthUser != null) ? saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser) : List.of();
        List<Purchase> allPurchases = (user != null) ? purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user) :
                (oauthUser != null) ? purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser) : List.of();
        List<Sale> completedSales = allSales.stream().filter(s -> s.getStatus() != null && s.getStatus().name().equalsIgnoreCase("COMPLETED")).collect(Collectors.toList());
        List<Purchase> completedPurchases = allPurchases.stream().filter(p -> p.getStatus() != null && p.getStatus().name().equalsIgnoreCase("COMPLETED")).collect(Collectors.toList());
        dto.setTotalSales(completedSales.size());
        dto.setTotalPurchases(completedPurchases.size());
        // Recent Activity
        List<String> recentActivities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int maxActivities = 5;
        for (int i = 0; i < Math.min(maxActivities, completedSales.size()); i++) {
            Sale sale = completedSales.get(i);
            recentActivities.add("Sale: " + sale.getProduct().getName() + " - Qty: " + sale.getQuantity() + " on " + sale.getSalesDate().format(formatter));
        }
        for (int i = 0; i < Math.min(maxActivities, completedPurchases.size()); i++) {
            Purchase purchase = completedPurchases.get(i);
            recentActivities.add("Purchase: " + purchase.getProduct().getName() + " - Qty: " + purchase.getQuantity() + " on " + purchase.getPurchaseDate().format(formatter));
        }
        dto.setRecentActivities(recentActivities);
        // Chart Data (accurate: Inventory Level Over Time)
        Map<String, Object> inventoryChart = new HashMap<>();
        List<String> months = new ArrayList<>();
        List<Integer> inventoryLevels = new ArrayList<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        // Get all products and their current quantity
        int currentTotalInventory = (user != null)
            ? productRepository.findAllByUserOrderByNameAsc(user).stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum()
            : (oauthUser != null)
                ? productRepository.findAllByOauthUserOrderByNameAsc(oauthUser).stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum()
                : 0;
        // For each of the last 6 months, calculate inventory at the end of that month
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate month = now.minusMonths(i);
            String label = month.getMonth().toString().substring(0, 3) + " '" + String.valueOf(month.getYear()).substring(2);
            months.add(label);
            // Inventory at end of this month = current inventory
            // + all sales after this month (add back)
            // - all purchases after this month (remove)
            int salesAfter = completedSales.stream().filter(s -> s.getSalesDate().isAfter(month.withDayOfMonth(month.lengthOfMonth()))).mapToInt(Sale::getQuantity).sum();
            int purchasesAfter = completedPurchases.stream().filter(p -> p.getPurchaseDate().isAfter(month.withDayOfMonth(month.lengthOfMonth()))).mapToInt(Purchase::getQuantity).sum();
            int inventoryAtMonth = currentTotalInventory + salesAfter - purchasesAfter;
            inventoryLevels.add(inventoryAtMonth);
        }
        inventoryChart.put("labels", months);
        inventoryChart.put("data", inventoryLevels);
        dto.setInventoryChartData(inventoryChart);
        Map<String, Object> salesPurchasesChart = new HashMap<>();
        salesPurchasesChart.put("labels", List.of("Sales", "Purchases"));
        salesPurchasesChart.put("data", List.of(completedSales.size(), completedPurchases.size()));
        dto.setSalesPurchasesChartData(salesPurchasesChart);
        Map<String, Object> topProductsChart = new HashMap<>();
        // Top 5 products by total quantity sold (completed sales only)
        Map<String, Long> productSalesCount = completedSales.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getName(), Collectors.summingLong(Sale::getQuantity)));
        List<Map.Entry<String, Long>> topProducts = productSalesCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
        topProductsChart.put("labels", topProducts.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        topProductsChart.put("data", topProducts.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        dto.setTopProductsChartData(topProductsChart);
        return dto;
    }
}


