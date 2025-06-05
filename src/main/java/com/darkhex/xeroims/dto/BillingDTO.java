package com.darkhex.xeroims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingDTO {
    private Long id;
    private LocalDate recordDate;
    private YearMonth monthYear;
    private Integer pendingSalesCount;
    private BigDecimal pendingSalesAmount;
    private Integer pendingPurchasesCount;
    private BigDecimal pendingPurchasesAmount;
    private BigDecimal balance;
    private BigDecimal profit;
    private BigDecimal expenses;
    private BigDecimal profitPercentageChange;
    private BigDecimal expensesPercentageChange;
    private BigDecimal balancePercentageChange;
    private Long userId;
    private Long oauthUserId;

    public BigDecimal calculateTotalTransactions() {
        BigDecimal salesAmount = pendingSalesAmount != null ? pendingSalesAmount : BigDecimal.ZERO;
        BigDecimal purchasesAmount = pendingPurchasesAmount != null ? pendingPurchasesAmount : BigDecimal.ZERO;
        return salesAmount.add(purchasesAmount);
    }
}
