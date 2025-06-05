package com.darkhex.xeroims.controller;

import com.darkhex.xeroims.dto.PurchaseDTO;
import com.darkhex.xeroims.dto.SaleDTO;
import com.darkhex.xeroims.enums.Status;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.service.BillingService;
import com.darkhex.xeroims.service.CustomerService;
import com.darkhex.xeroims.service.PurchaseService;
import com.darkhex.xeroims.service.SaleService;
import com.darkhex.xeroims.service.SupplierService;
import com.darkhex.xeroims.service.UserService;
import com.darkhex.xeroims.service.OauthUserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final SupplierService supplierService;
    private final CustomerService customerService;
    private final UserService userService;
    private final OauthUserService oauthUserService;

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    private OauthUser getOauthUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return oauthUserService.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public String showBillingPage(
            @RequestParam(name = "page", defaultValue = "0") int purchasePage,
            @RequestParam(name = "pageSales", defaultValue = "0") int salesPage,
            @RequestParam(name = "searchPurchases", required = false) String searchPurchases,
            @RequestParam(name = "searchSales", required = false) String searchSales,
            @RequestParam(name = "purchaseDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateFrom,
            @RequestParam(name = "purchaseDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateTo,
            @RequestParam(name = "salesDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salesDateFrom,
            @RequestParam(name = "salesDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salesDateTo,
            @RequestParam(name = "purchaseStatus", required = false) String purchaseStatus,
            @RequestParam(name = "salesStatus", required = false) String salesStatus,
            @RequestParam(name = "supplierId", required = false) Long supplierId,
            @RequestParam(name = "customerId", required = false) Long customerId,
            Model model,
            Authentication authentication,
            @ModelAttribute("successMessage") String successMessage) {

        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        BillingService.BillingDashboardMetrics metrics = billingService.getFinancialDashboardMetrics(user, oauthUser);
        model.addAttribute("pendingSalesCount", metrics.getPendingSalesCount());
        model.addAttribute("pendingSalesAmount", metrics.getPendingSalesAmount());
        model.addAttribute("pendingPurchasesCount", metrics.getPendingPurchasesCount());
        model.addAttribute("pendingPurchasesAmount", metrics.getPendingPurchasesAmount());
        model.addAttribute("balance", metrics.getBalance());
        model.addAttribute("profit", metrics.getProfit());
        model.addAttribute("expenses", metrics.getExpenses());

        try {
            model.addAttribute("suppliers", supplierService.getAllSuppliers(user, oauthUser));
            model.addAttribute("customers", customerService.getAllCustomers(user, oauthUser));
        } catch (Exception e) {
            model.addAttribute("suppliers", Collections.emptyList());
            model.addAttribute("customers", Collections.emptyList());
            model.addAttribute("errorMessage", "Failed to load suppliers or customers: " + e.getMessage());
        }

        Pageable purchasePageable = PageRequest.of(purchasePage, 10, Sort.by(Sort.Direction.DESC, "purchaseDate"));
        Page<PurchaseDTO> purchasePageResult = getFilteredPurchases(
                user, oauthUser, searchPurchases, purchaseDateFrom, purchaseDateTo, purchaseStatus, supplierId, purchasePageable);
        model.addAttribute("purchases", purchasePageResult.getContent());
        model.addAttribute("currentPurchasePage", purchasePage);
        model.addAttribute("totalPurchasePages", purchasePageResult.getTotalPages());

        Pageable salesPageable = PageRequest.of(salesPage, 10, Sort.by(Sort.Direction.DESC, "salesDate"));
        Page<SaleDTO> salesPageResult = getFilteredSales(
                user, oauthUser, searchSales, salesDateFrom, salesDateTo, salesStatus, customerId, salesPageable);
        model.addAttribute("sales", salesPageResult.getContent());
        model.addAttribute("currentSalesPage", salesPage);
        model.addAttribute("totalSalesPages", salesPageResult.getTotalPages());

        model.addAttribute("searchPurchases", searchPurchases);
        model.addAttribute("searchSales", searchSales);

        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }

        return "billing";
    }

    @PostMapping("/approve-purchase")
    public String approvePurchase(
            @RequestParam("purchaseId") Long purchaseId,
            RedirectAttributes redirectAttributes) {
        try {
            boolean success = billingService.updatePurchaseStatus(purchaseId, Status.COMPLETED);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Purchase approved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve purchase. It may no longer be pending.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving purchase: " + e.getMessage());
        }
        return "redirect:/billing";
    }

    @PostMapping("/cancel-purchase")
    public String cancelPurchase(
            @RequestParam("purchaseId") Long purchaseId,
            RedirectAttributes redirectAttributes) {
        try {
            boolean success = billingService.updatePurchaseStatus(purchaseId, Status.CANCELLED);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Purchase cancelled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel purchase. It may no longer be pending.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling purchase: " + e.getMessage());
        }
        return "redirect:/billing";
    }

    @PostMapping("/approve-sale")
    public String approveSale(
            @RequestParam("saleId") Long saleId,
            RedirectAttributes redirectAttributes) {
        try {
            boolean success = billingService.updateSaleStatus(saleId, Status.COMPLETED);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Sale approved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve sale. It may no longer be pending or there may be insufficient stock.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving sale: " + e.getMessage());
        }
        return "redirect:/billing";
    }

    @PostMapping("/cancel-sale")
    public String cancelSale(
            @RequestParam("saleId") Long saleId,
            RedirectAttributes redirectAttributes) {
        try {
            boolean success = billingService.updateSaleStatus(saleId, Status.CANCELLED);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Sale cancelled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel sale. It may no longer be pending.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling sale: " + e.getMessage());
        }
        return "redirect:/billing";
    }

    @GetMapping("/export-purchases")
    public void exportPurchases(
            HttpServletResponse response,
            @RequestParam(name = "searchPurchases", required = false) String searchPurchases,
            @RequestParam(name = "purchaseDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateFrom,
            @RequestParam(name = "purchaseDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateTo,
            @RequestParam(name = "purchaseStatus", required = false) String purchaseStatus,
            @RequestParam(name = "supplierId", required = false) Long supplierId,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "purchaseDate"));
        Page<PurchaseDTO> purchasePage = getFilteredPurchases(user, oauthUser, searchPurchases, purchaseDateFrom, purchaseDateTo, purchaseStatus, supplierId, pageable);
        List<PurchaseDTO> purchases = purchasePage.getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Purchases");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Date", "Product", "Supplier", "Quantity", "Total Amount", "Status"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        int rowNum = 1;
        for (PurchaseDTO purchase : purchases) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(purchase.getId());
            row.createCell(1).setCellValue(purchase.getPurchaseDate().format(dateFormatter));
            row.createCell(2).setCellValue(purchase.getProductName());
            row.createCell(3).setCellValue(purchase.getSupplierName());
            row.createCell(4).setCellValue(purchase.getQuantity());
            row.createCell(5).setCellValue(purchase.getTotalPrice().toString());
            row.createCell(6).setCellValue(purchase.getStatus().toString());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=purchases.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/export-sales")
    public void exportSales(
            HttpServletResponse response,
            @RequestParam(name = "searchSales", required = false) String searchSales,
            @RequestParam(name = "salesDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salesDateFrom,
            @RequestParam(name = "salesDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salesDateTo,
            @RequestParam(name = "salesStatus", required = false) String salesStatus,
            @RequestParam(name = "customerId", required = false) Long customerId,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        OauthUser oauthUser = getOauthUser(authentication);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "salesDate"));
        Page<SaleDTO> salesPage = getFilteredSales(user, oauthUser, searchSales, salesDateFrom, salesDateTo, salesStatus, customerId, pageable);
        List<SaleDTO> sales = salesPage.getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Date", "Product", "Customer", "Quantity", "Total Amount", "Status"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        int rowNum = 1;
        for (SaleDTO sale : sales) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sale.getId());
            row.createCell(1).setCellValue(sale.getSalesDate().format(dateFormatter));
            row.createCell(2).setCellValue(sale.getProductName());
            row.createCell(3).setCellValue(sale.getCustomerName());
            row.createCell(4).setCellValue(sale.getQuantity());
            row.createCell(5).setCellValue(sale.getTotalPrice().toString());
            row.createCell(6).setCellValue(sale.getStatus().toString());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sales.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    private Page<PurchaseDTO> getFilteredPurchases(User user, OauthUser oauthUser, String search, LocalDate dateFrom, LocalDate dateTo, String status, Long supplierId, Pageable pageable) {
        List<PurchaseDTO> purchases = purchaseService.getAllPurchases(user, oauthUser).stream()
                .map(purchase -> {
                    PurchaseDTO dto = new PurchaseDTO(
                            purchase.getId(),
                            purchase.getProduct().getId(),
                            purchase.getProduct().getName(),
                            purchase.getSupplier().getId(),
                            purchase.getSupplier().getName(),
                            purchase.getQuantity(),
                            purchase.getTotalPrice(),
                            purchase.getPurchaseDate(),
                            purchase.getStatus()
                    );
                    return dto;
                })
                .collect(Collectors.toList());

        List<PurchaseDTO> filteredPurchases = purchases.stream()
                .filter(purchase -> {
                    boolean matches = true;
                    if (search != null && !search.trim().isEmpty()) {
                        matches = purchase.getProductName().toLowerCase().contains(search.toLowerCase()) ||
                                purchase.getSupplierName().toLowerCase().contains(search.toLowerCase());
                    }
                    if (dateFrom != null) {
                        matches = matches && !purchase.getPurchaseDate().isBefore(dateFrom);
                    }
                    if (dateTo != null) {
                        matches = matches && !purchase.getPurchaseDate().isAfter(dateTo);
                    }
                    if (status != null && !status.isEmpty()) {
                        matches = matches && purchase.getStatus().name().equals(status);
                    }
                    if (supplierId != null) {
                        matches = matches && purchase.getSupplierId().equals(supplierId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        int totalPages = filteredPurchases.isEmpty() ? 1 : (int) Math.ceil((double) filteredPurchases.size() / pageSize);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredPurchases.size());

        if (start >= filteredPurchases.size() && !filteredPurchases.isEmpty()) {
            int lastPageStart = Math.max(0, filteredPurchases.size() - pageable.getPageSize());
            start = lastPageStart;
            end = filteredPurchases.size();
        }

        List<PurchaseDTO> pagedPurchases = filteredPurchases.isEmpty() ?
            filteredPurchases : filteredPurchases.subList(start, end);

        return new PageImpl<>(pagedPurchases, pageable, filteredPurchases.size());
    }

    private Page<SaleDTO> getFilteredSales(User user, OauthUser oauthUser, String search, LocalDate dateFrom, LocalDate dateTo, String status, Long customerId, Pageable pageable) {
        List<SaleDTO> sales = saleService.getAllSales(user, oauthUser).stream()
                .map(sale -> {
                    SaleDTO dto = new SaleDTO(
                            sale.getId(),
                            sale.getProduct().getId(),
                            sale.getProduct().getName(),
                            sale.getCustomerName(),
                            sale.getQuantity(),
                            sale.getPrice(),
                            sale.getTotalPrice(),
                            sale.getSalesDate(),
                            sale.getStatus()
                    );
                    return dto;
                })
                .collect(Collectors.toList());

        List<SaleDTO> filteredSales = sales.stream()
                .filter(sale -> {
                    boolean matches = true;
                    if (search != null && !search.trim().isEmpty()) {
                        matches = sale.getProductName().toLowerCase().contains(search.toLowerCase()) ||
                                sale.getCustomerName().toLowerCase().contains(search.toLowerCase());
                    }
                    if (dateFrom != null) {
                        matches = matches && !sale.getSalesDate().isBefore(dateFrom);
                    }
                    if (dateTo != null) {
                        matches = matches && !sale.getSalesDate().isAfter(dateTo);
                    }
                    if (status != null && !status.isEmpty()) {
                        matches = matches && sale.getStatus().name().equals(status);
                    }
                    if (customerId != null) {
                        matches = matches && true;
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        int totalPages = filteredSales.isEmpty() ? 1 : (int) Math.ceil((double) filteredSales.size() / pageSize);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredSales.size());

        if (start >= filteredSales.size() && !filteredSales.isEmpty()) {
            int lastPageStart = Math.max(0, filteredSales.size() - pageable.getPageSize());
            start = lastPageStart;
            end = filteredSales.size();
        }

        List<SaleDTO> pagedSales = filteredSales.isEmpty() ?
            filteredSales : filteredSales.subList(start, end);

        return new PageImpl<>(pagedSales, pageable, filteredSales.size());
    }
}
