package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.BillingDTO;
import com.darkhex.xeroims.enums.Status;
import com.darkhex.xeroims.model.*;
import com.darkhex.xeroims.repository.BillingRepository;
import com.darkhex.xeroims.repository.PurchaseRepository;
import com.darkhex.xeroims.repository.SaleRepository;
import com.darkhex.xeroims.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;


    public BigDecimal calculateCurrentBalance(User user) {
        if (user != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByUserOrderByRecordDateDesc(user);
            return latestBilling.map(Billing::getBalance).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal calculateCurrentBalance(OauthUser oauthUser) {
        if (oauthUser != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
            return latestBilling.map(Billing::getBalance).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateCurrentProfit(User user) {
        if (user != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByUserOrderByRecordDateDesc(user);
            return latestBilling.map(Billing::getProfit).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal calculateCurrentProfit(OauthUser oauthUser) {
        if (oauthUser != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
            return latestBilling.map(Billing::getProfit).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal calculateCurrentExpenses(User user) {
        if (user != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByUserOrderByRecordDateDesc(user);
            return latestBilling.map(Billing::getExpenses).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal calculateCurrentExpenses(OauthUser oauthUser) {
        if (oauthUser != null) {
            Optional<Billing> latestBilling = billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
            return latestBilling.map(Billing::getExpenses).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public Billing createOrUpdateDailyRecord(User user) {
        return createOrUpdateDailyRecordInternal(user, null);
    }


    @Transactional
    public Billing createOrUpdateDailyRecord(OauthUser oauthUser) {
        return createOrUpdateDailyRecordInternal(null, oauthUser);
    }

    private Billing createOrUpdateDailyRecordInternal(User user, OauthUser oauthUser) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        int pendingSalesCount = countPendingSales(user, oauthUser);
        BigDecimal pendingSalesAmount = calculatePendingSalesAmount(user, oauthUser);

        int pendingPurchasesCount = countPendingPurchases(user, oauthUser);
        BigDecimal pendingPurchasesAmount = calculatePendingPurchasesAmount(user, oauthUser);

        BigDecimal balance = calculateBalanceForDate(today, user, oauthUser);
        BigDecimal profit = calculateProfitForDate(today, user, oauthUser);
        BigDecimal expenses = calculateExpensesForDate(today, user, oauthUser);

        YearMonth previousMonth = currentMonth.minusMonths(1);
        BigDecimal previousProfit = getPreviousMonthMetric(previousMonth, "profit", user, oauthUser);
        BigDecimal previousExpenses = getPreviousMonthMetric(previousMonth, "expenses", user, oauthUser);
        BigDecimal previousBalance = getPreviousMonthMetric(previousMonth, "balance", user, oauthUser);

        BigDecimal profitPercentageChange = calculatePercentageChange(profit, previousProfit);
        BigDecimal expensesPercentageChange = calculatePercentageChange(expenses, previousExpenses);
        BigDecimal balancePercentageChange = calculatePercentageChange(balance, previousBalance);

        List<Billing> todayRecords;
        if (user != null) {
            todayRecords = billingRepository.findByRecordDateBetweenAndUserOrderByRecordDateDesc(
                    today, today, user);
        } else {
            todayRecords = billingRepository.findByRecordDateBetweenAndOauthUserOrderByRecordDateDesc(
                    today, today, oauthUser);
        }

        Billing billing;
        if (!todayRecords.isEmpty()) {
            billing = todayRecords.get(0);
            billing.setPendingSalesCount(pendingSalesCount);
            billing.setPendingSalesAmount(pendingSalesAmount);
            billing.setPendingPurchasesCount(pendingPurchasesCount);
            billing.setPendingPurchasesAmount(pendingPurchasesAmount);
            billing.setBalance(balance);
            billing.setProfit(profit);
            billing.setExpenses(expenses);
            billing.setProfitPercentageChange(profitPercentageChange);
            billing.setExpensesPercentageChange(expensesPercentageChange);
            billing.setBalancePercentageChange(balancePercentageChange);
        } else {
            billing = new Billing(
                    today, currentMonth,
                    pendingSalesCount, pendingSalesAmount,
                    pendingPurchasesCount, pendingPurchasesAmount,
                    balance, profit, expenses,
                    profitPercentageChange, expensesPercentageChange, balancePercentageChange,
                    user, oauthUser
            );
        }

        return billingRepository.save(billing);
    }


    public int countPendingSales(User user, OauthUser oauthUser) {
        List<Sale> pendingSales;

        if (user != null) {
            pendingSales = saleRepository.findAllByUserOrderBySalesDateDesc(user)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            pendingSales = saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else {
            return 0;
        }

        return pendingSales.size();
    }


    public BigDecimal calculatePendingSalesAmount(User user, OauthUser oauthUser) {
        List<Sale> pendingSales;

        if (user != null) {
            pendingSales = saleRepository.findAllByUserOrderBySalesDateDesc(user)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            pendingSales = saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else {
            return BigDecimal.ZERO;
        }

        return pendingSales.stream()
                .map(Sale::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public int countPendingPurchases(User user, OauthUser oauthUser) {
        List<Purchase> pendingPurchases;

        if (user != null) {
            pendingPurchases = purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            pendingPurchases = purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else {
            return 0;
        }

        return pendingPurchases.size();
    }


    public BigDecimal calculatePendingPurchasesAmount(User user, OauthUser oauthUser) {
        List<Purchase> pendingPurchases;

        if (user != null) {
            pendingPurchases = purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            pendingPurchases = purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
        } else {
            return BigDecimal.ZERO;
        }

        return pendingPurchases.stream()
                .map(Purchase::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateBalanceForDate(LocalDate date, User user, OauthUser oauthUser) {
        List<Sale> completedSales;
        List<Purchase> completedPurchases;

        if (user != null) {
            completedSales = saleRepository.findAllByUserOrderBySalesDateDesc(user)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.COMPLETED && !sale.getSalesDate().isAfter(date))
                    .collect(Collectors.toList());

            completedPurchases = purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            completedSales = saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.COMPLETED && !sale.getSalesDate().isAfter(date))
                    .collect(Collectors.toList());

            completedPurchases = purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSales = completedSales.stream()
                .map(Sale::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchases = completedPurchases.stream()
                .map(Purchase::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSales.subtract(totalPurchases);
    }

    private BigDecimal calculateProfitForDate(LocalDate date, User user, OauthUser oauthUser) {
        List<Sale> completedSales;
        List<Purchase> completedPurchases;

        if (user != null) {
            completedSales = saleRepository.findAllByUserOrderBySalesDateDesc(user)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.COMPLETED && !sale.getSalesDate().isAfter(date))
                    .collect(Collectors.toList());

            completedPurchases = purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            completedSales = saleRepository.findAllByOauthUserOrderBySalesDateDesc(oauthUser)
                    .stream()
                    .filter(sale -> sale.getStatus() == Status.COMPLETED && !sale.getSalesDate().isAfter(date))
                    .collect(Collectors.toList());

            completedPurchases = purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSalesAmount = completedSales.stream()
                .map(Sale::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchasesAmount = completedPurchases.stream()
                .map(Purchase::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate profit as sales minus purchases
        return totalSalesAmount.subtract(totalPurchasesAmount);
    }

    private BigDecimal calculateExpensesForDate(LocalDate date, User user, OauthUser oauthUser) {
        List<Purchase> completedPurchases;

        if (user != null) {
            completedPurchases = purchaseRepository.findAllByUserOrderByPurchaseDateDesc(user)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else if (oauthUser != null) {
            completedPurchases = purchaseRepository.findAllByOauthUserOrderByPurchaseDateDesc(oauthUser)
                    .stream()
                    .filter(purchase -> purchase.getStatus() == Status.COMPLETED && !purchase.getPurchaseDate().isAfter(date))
                    .collect(Collectors.toList());
        } else {
            return BigDecimal.ZERO;
        }

        return completedPurchases.stream()
                .map(Purchase::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPreviousMonthMetric(YearMonth previousMonth, String metricType, User user, OauthUser oauthUser) {
        Optional<Billing> previousMonthBilling = Optional.empty();

        if (user != null) {
            previousMonthBilling = billingRepository.findByMonthYearAndUser(previousMonth, user)
                    .stream().findFirst();
        } else if (oauthUser != null) {
            previousMonthBilling = billingRepository.findByMonthYearAndOauthUser(previousMonth, oauthUser)
                    .stream().findFirst();
        }

        if (previousMonthBilling.isPresent()) {
            Billing billing = previousMonthBilling.get();
            switch (metricType) {
                case "profit":
                    return billing.getProfit();
                case "expenses":
                    return billing.getExpenses();
                case "balance":
                    return billing.getBalance();
                default:
                    return BigDecimal.ZERO;
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BillingDTO convertToDto(Billing billing) {
        if (billing == null) {
            return null;
        }

        BillingDTO dto = new BillingDTO();
        dto.setId(billing.getId());
        dto.setRecordDate(billing.getRecordDate());
        dto.setMonthYear(billing.getMonthYear());
        dto.setPendingSalesCount(billing.getPendingSalesCount());
        dto.setPendingSalesAmount(billing.getPendingSalesAmount());
        dto.setPendingPurchasesCount(billing.getPendingPurchasesCount());
        dto.setPendingPurchasesAmount(billing.getPendingPurchasesAmount());
        dto.setBalance(billing.getBalance());
        dto.setProfit(billing.getProfit());
        dto.setExpenses(billing.getExpenses());
        dto.setProfitPercentageChange(billing.getProfitPercentageChange());
        dto.setExpensesPercentageChange(billing.getExpensesPercentageChange());
        dto.setBalancePercentageChange(billing.getBalancePercentageChange());

        if (billing.getUser() != null) {
            dto.setUserId(billing.getUser().getId());
        }

        if (billing.getOauthUser() != null) {
            try {
                dto.setOauthUserId(Long.valueOf(billing.getOauthUser().getId()));
            } catch (NumberFormatException e) {
                dto.setOauthUserId(null);
            }
        }

        return dto;
    }

    public List<BillingDTO> findAllForUser(User user) {
        List<Billing> billings = billingRepository.findByUserOrderByRecordDateDesc(user);
        return billings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<BillingDTO> findAllForOauthUser(OauthUser oauthUser) {
        List<Billing> billings = billingRepository.findByOauthUserOrderByRecordDateDesc(oauthUser);
        return billings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<BillingDTO> findByDateRangeForUser(LocalDate startDate, LocalDate endDate, User user) {
        List<Billing> billings = billingRepository.findByRecordDateBetweenAndUserOrderByRecordDateDesc(
                startDate, endDate, user);
        return billings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<BillingDTO> findByDateRangeForOauthUser(LocalDate startDate, LocalDate endDate, OauthUser oauthUser) {
        List<Billing> billings = billingRepository.findByRecordDateBetweenAndOauthUserOrderByRecordDateDesc(
                startDate, endDate, oauthUser);
        return billings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<YearMonth> findAllMonthYearsForUser(User user) {
        return billingRepository.findDistinctMonthYearsByUser(user);
    }

    public List<YearMonth> findAllMonthYearsForOauthUser(OauthUser oauthUser) {
        return billingRepository.findDistinctMonthYearsByOauthUser(oauthUser);
    }
    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) { super(message); }
    }
    public static class InsufficientProductException extends RuntimeException {
        public InsufficientProductException(String message) { super(message); }
    }

    @Transactional
    public boolean updatePurchaseStatus(Long purchaseId, Status status) {
        if (purchaseId == null || status == null) {
            return false;
        }

        Optional<Purchase> purchaseOpt = purchaseRepository.findById(purchaseId);

        if (purchaseOpt.isPresent()) {
            Purchase purchase = purchaseOpt.get();

            if (purchase.getStatus() == Status.PENDING) {
                if (status == Status.COMPLETED) {
                    User user = purchase.getUser();
                    OauthUser oauthUser = purchase.getOauthUser();
                    BigDecimal currentBalance = user != null ? calculateCurrentBalance(user) : calculateCurrentBalance(oauthUser);
                    BigDecimal amount = purchase.getTotalPrice();
                    if (currentBalance.compareTo(amount) < 0) {
                        throw new InsufficientFundsException("Insufficient Funds");
                    }
                }
                purchase.setStatus(status);
                purchaseRepository.save(purchase);

                if (status == Status.COMPLETED) {
                    updateProductStockForPurchaseApproval(purchase);
                    // Update billing cards
                    User user = purchase.getUser();
                    OauthUser oauthUser = purchase.getOauthUser();
                    Optional<Billing> latestBilling = user != null
                        ? billingRepository.findFirstByUserOrderByRecordDateDesc(user)
                        : billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
                    BigDecimal amount = purchase.getTotalPrice();
                    BigDecimal prevBalance = latestBilling.map(Billing::getBalance).orElse(BigDecimal.ZERO);
                    BigDecimal prevProfit = latestBilling.map(Billing::getProfit).orElse(BigDecimal.ZERO);
                    BigDecimal prevExpenses = latestBilling.map(Billing::getExpenses).orElse(BigDecimal.ZERO);
                    Billing newBilling = new Billing();
                    newBilling.setRecordDate(LocalDate.now());
                    newBilling.setMonthYear(YearMonth.now());
                    newBilling.setBalance(prevBalance.subtract(amount));
                    newBilling.setProfit(prevProfit.subtract(amount));
                    newBilling.setExpenses(prevExpenses.add(amount));
                    newBilling.setPendingSalesCount(latestBilling.map(Billing::getPendingSalesCount).orElse(0));
                    newBilling.setPendingSalesAmount(latestBilling.map(Billing::getPendingSalesAmount).orElse(BigDecimal.ZERO));
                    newBilling.setPendingPurchasesCount(latestBilling.map(Billing::getPendingPurchasesCount).orElse(0));
                    newBilling.setPendingPurchasesAmount(latestBilling.map(Billing::getPendingPurchasesAmount).orElse(BigDecimal.ZERO));
                    if (user != null) newBilling.setUser(user);
                    if (oauthUser != null) newBilling.setOauthUser(oauthUser);
                    billingRepository.save(newBilling);
                }

                return true;
            }
        }

        return false;
    }

    @Transactional
    public boolean updateSaleStatus(Long saleId, Status status) {
        if (saleId == null || status == null) {
            return false;
        }

        Optional<Sale> saleOpt = saleRepository.findById(saleId);

        if (saleOpt.isPresent()) {
            Sale sale = saleOpt.get();

            if (sale.getStatus() == Status.PENDING) {
                if (status == Status.COMPLETED) {
                    if (!hasEnoughStockForSale(sale)) {
                        throw new InsufficientProductException("Insufficient Product");
                    }
                    updateProductStockForSaleApproval(sale);
                    // Update billing cards
                    User user = sale.getUser();
                    OauthUser oauthUser = sale.getOauthUser();
                    Optional<Billing> latestBilling = user != null
                        ? billingRepository.findFirstByUserOrderByRecordDateDesc(user)
                        : billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
                    BigDecimal amount = sale.getTotalPrice();
                    BigDecimal prevBalance = latestBilling.map(Billing::getBalance).orElse(BigDecimal.ZERO);
                    BigDecimal prevProfit = latestBilling.map(Billing::getProfit).orElse(BigDecimal.ZERO);
                    BigDecimal prevExpenses = latestBilling.map(Billing::getExpenses).orElse(BigDecimal.ZERO);
                    Billing newBilling = new Billing();
                    newBilling.setRecordDate(LocalDate.now());
                    newBilling.setMonthYear(YearMonth.now());
                    newBilling.setBalance(prevBalance.add(amount));
                    newBilling.setProfit(prevProfit.add(amount));
                    newBilling.setExpenses(prevExpenses); // no change
                    newBilling.setPendingSalesCount(latestBilling.map(Billing::getPendingSalesCount).orElse(0));
                    newBilling.setPendingSalesAmount(latestBilling.map(Billing::getPendingSalesAmount).orElse(BigDecimal.ZERO));
                    newBilling.setPendingPurchasesCount(latestBilling.map(Billing::getPendingPurchasesCount).orElse(0));
                    newBilling.setPendingPurchasesAmount(latestBilling.map(Billing::getPendingPurchasesAmount).orElse(BigDecimal.ZERO));
                    if (user != null) newBilling.setUser(user);
                    if (oauthUser != null) newBilling.setOauthUser(oauthUser);
                    billingRepository.save(newBilling);
                }

                sale.setStatus(status);
                saleRepository.save(sale);

                return true;
            }
        }

        return false;
    }


    private boolean hasEnoughStockForSale(Sale sale) {
        Product product = sale.getProduct();
        if (product != null) {
            Integer currentQty = product.getQuantity();
            if (currentQty == null) {
                currentQty = 0;
            }
            return currentQty >= sale.getQuantity();
        }
        return false;
    }

    @Transactional
    protected void updateProductStockForSaleApproval(Sale sale) {
        Product product = sale.getProduct();
        if (product != null) {
            Integer currentQty = product.getQuantity();
            if (currentQty == null) {
                currentQty = 0;
            }
            product.setQuantity(currentQty - sale.getQuantity());

            productRepository.save(product);
        }
    }

    @Transactional
    protected void updateProductStockForPurchaseApproval(Purchase purchase) {
        Product product = purchase.getProduct();
        if (product != null) {
            Integer currentQty = product.getQuantity();
            if (currentQty == null) {
                currentQty = 0;
            }
            product.setQuantity(currentQty + purchase.getQuantity());
            productRepository.save(product);
        }
    }


    public BillingDashboardMetrics getFinancialDashboardMetrics(User user, OauthUser oauthUser) {
        BillingDashboardMetrics metrics = new BillingDashboardMetrics();

        metrics.setPendingSalesCount(countPendingSales(user, oauthUser));
        metrics.setPendingSalesAmount(calculatePendingSalesAmount(user, oauthUser));
        metrics.setPendingPurchasesCount(countPendingPurchases(user, oauthUser));
        metrics.setPendingPurchasesAmount(calculatePendingPurchasesAmount(user, oauthUser));

        // Use the latest Billing record's balance, profit, and expenses
        Optional<Billing> latestBilling = user != null
            ? billingRepository.findFirstByUserOrderByRecordDateDesc(user)
            : billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser);
        if (latestBilling.isPresent()) {
            Billing billing = latestBilling.get();
            metrics.setBalance(billing.getBalance());
            metrics.setProfit(billing.getProfit());
            metrics.setExpenses(billing.getExpenses());
        } else {
            metrics.setBalance(BigDecimal.ZERO);
            metrics.setProfit(BigDecimal.ZERO);
            metrics.setExpenses(BigDecimal.ZERO);
        }

        // Percentage changes can remain as before
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal previousProfit = getPreviousMonthMetric(previousMonth, "profit", user, oauthUser);
        BigDecimal previousExpenses = getPreviousMonthMetric(previousMonth, "expenses", user, oauthUser);
        BigDecimal previousBalance = getPreviousMonthMetric(previousMonth, "balance", user, oauthUser);

        metrics.setProfitPercentageChange(calculatePercentageChange(metrics.getProfit(), previousProfit));
        metrics.setExpensesPercentageChange(calculatePercentageChange(metrics.getExpenses(), previousExpenses));
        metrics.setBalancePercentageChange(calculatePercentageChange(metrics.getBalance(), previousBalance));

        return metrics;
    }

    public static class BillingDashboardMetrics {
        private int pendingSalesCount;
        private BigDecimal pendingSalesAmount = BigDecimal.ZERO;
        private int pendingPurchasesCount;
        private BigDecimal pendingPurchasesAmount = BigDecimal.ZERO;
        private BigDecimal balance = BigDecimal.ZERO;
        private BigDecimal profit = BigDecimal.ZERO;
        private BigDecimal expenses = BigDecimal.ZERO;
        private BigDecimal profitPercentageChange = BigDecimal.ZERO;
        private BigDecimal expensesPercentageChange = BigDecimal.ZERO;
        private BigDecimal balancePercentageChange = BigDecimal.ZERO;

        public int getPendingSalesCount() {
            return pendingSalesCount;
        }

        public void setPendingSalesCount(int pendingSalesCount) {
            this.pendingSalesCount = pendingSalesCount;
        }

        public BigDecimal getPendingSalesAmount() {
            return pendingSalesAmount;
        }

        public void setPendingSalesAmount(BigDecimal pendingSalesAmount) {
            this.pendingSalesAmount = pendingSalesAmount;
        }

        public int getPendingPurchasesCount() {
            return pendingPurchasesCount;
        }

        public void setPendingPurchasesCount(int pendingPurchasesCount) {
            this.pendingPurchasesCount = pendingPurchasesCount;
        }

        public BigDecimal getPendingPurchasesAmount() {
            return pendingPurchasesAmount;
        }

        public void setPendingPurchasesAmount(BigDecimal pendingPurchasesAmount) {
            this.pendingPurchasesAmount = pendingPurchasesAmount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getProfit() {
            return profit;
        }

        public void setProfit(BigDecimal profit) {
            this.profit = profit;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }

        public void setExpenses(BigDecimal expenses) {
            this.expenses = expenses;
        }

        public BigDecimal getProfitPercentageChange() {
            return profitPercentageChange;
        }

        public void setProfitPercentageChange(BigDecimal profitPercentageChange) {
            this.profitPercentageChange = profitPercentageChange;
        }

        public BigDecimal getExpensesPercentageChange() {
            return expensesPercentageChange;
        }

        public void setExpensesPercentageChange(BigDecimal expensesPercentageChange) {
            this.expensesPercentageChange = expensesPercentageChange;
        }

        public BigDecimal getBalancePercentageChange() {
            return balancePercentageChange;
        }

        public void setBalancePercentageChange(BigDecimal balancePercentageChange) {
            this.balancePercentageChange = balancePercentageChange;
        }
    }

    @Transactional
    public void addBalance(User user, OauthUser oauthUser, BigDecimal amount) {
        if (user == null && oauthUser == null) return;
        Billing latestBilling = null;
        if (user != null) {
            latestBilling = billingRepository.findFirstByUserOrderByRecordDateDesc(user).orElse(null);
        } else if (oauthUser != null) {
            latestBilling = billingRepository.findFirstByOauthUserOrderByRecordDateDesc(oauthUser).orElse(null);
        }
        BigDecimal currentBalance = latestBilling != null && latestBilling.getBalance() != null ? latestBilling.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);
        Billing newBilling = new Billing();
        newBilling.setRecordDate(LocalDate.now());
        newBilling.setMonthYear(YearMonth.now());
        newBilling.setBalance(newBalance);
        newBilling.setProfit(latestBilling != null ? latestBilling.getProfit() : BigDecimal.ZERO);
        newBilling.setExpenses(latestBilling != null ? latestBilling.getExpenses() : BigDecimal.ZERO);
        newBilling.setPendingSalesCount(latestBilling != null ? latestBilling.getPendingSalesCount() : 0);
        newBilling.setPendingSalesAmount(latestBilling != null ? latestBilling.getPendingSalesAmount() : BigDecimal.ZERO);
        newBilling.setPendingPurchasesCount(latestBilling != null ? latestBilling.getPendingPurchasesCount() : 0);
        newBilling.setPendingPurchasesAmount(latestBilling != null ? latestBilling.getPendingPurchasesAmount() : BigDecimal.ZERO);
        if (user != null) newBilling.setUser(user);
        if (oauthUser != null) newBilling.setOauthUser(oauthUser);
        billingRepository.save(newBilling);
    }
}
