package com.darkhex.xeroims.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private long totalProducts;
    private long totalCategories;
    private long totalSuppliers;
    private long totalSales;
    private long totalPurchases;
    private List<String> recentActivities;
    // Chart data as generic maps (can be refined as needed)
    private Map<String, Object> inventoryChartData;
    private Map<String, Object> salesPurchasesChartData;
    private Map<String, Object> topProductsChartData;

    // Getters and setters
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
    public long getTotalSuppliers() { return totalSuppliers; }
    public void setTotalSuppliers(long totalSuppliers) { this.totalSuppliers = totalSuppliers; }
    public long getTotalSales() { return totalSales; }
    public void setTotalSales(long totalSales) { this.totalSales = totalSales; }
    public long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(long totalPurchases) { this.totalPurchases = totalPurchases; }
    public List<String> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<String> recentActivities) { this.recentActivities = recentActivities; }
    public Map<String, Object> getInventoryChartData() { return inventoryChartData; }
    public void setInventoryChartData(Map<String, Object> inventoryChartData) { this.inventoryChartData = inventoryChartData; }
    public Map<String, Object> getSalesPurchasesChartData() { return salesPurchasesChartData; }
    public void setSalesPurchasesChartData(Map<String, Object> salesPurchasesChartData) { this.salesPurchasesChartData = salesPurchasesChartData; }
    public Map<String, Object> getTopProductsChartData() { return topProductsChartData; }
    public void setTopProductsChartData(Map<String, Object> topProductsChartData) { this.topProductsChartData = topProductsChartData; }
}


