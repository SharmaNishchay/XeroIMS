package com.darkhex.xeroims.model;

import com.darkhex.xeroims.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "billing")
@Getter
@Setter
@NoArgsConstructor
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "month_year", nullable = false)
    private YearMonth monthYear;

    @Column(name = "pending_sales_count")
    private Integer pendingSalesCount;

    @Column(name = "pending_sales_amount", precision = 15, scale = 2)
    private BigDecimal pendingSalesAmount;

    @Column(name = "pending_purchases_count")
    private Integer pendingPurchasesCount;

    @Column(name = "pending_purchases_amount", precision = 15, scale = 2)
    private BigDecimal pendingPurchasesAmount;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "profit", precision = 15, scale = 2)
    private BigDecimal profit;

    @Column(name = "expenses", precision = 15, scale = 2)
    private BigDecimal expenses;

    @Column(name = "profit_percentage_change", precision = 10, scale = 2)
    private BigDecimal profitPercentageChange;

    @Column(name = "expenses_percentage_change", precision = 10, scale = 2)
    private BigDecimal expensesPercentageChange;

    @Column(name = "balance_percentage_change", precision = 10, scale = 2)
    private BigDecimal balancePercentageChange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_user_id")
    private OauthUser oauthUser;

    public Billing(LocalDate recordDate, YearMonth monthYear,
                  Integer pendingSalesCount, BigDecimal pendingSalesAmount,
                  Integer pendingPurchasesCount, BigDecimal pendingPurchasesAmount,
                  BigDecimal balance, BigDecimal profit, BigDecimal expenses,
                  BigDecimal profitPercentageChange, BigDecimal expensesPercentageChange,
                  BigDecimal balancePercentageChange,
                  User user, OauthUser oauthUser) {
        this.recordDate = recordDate;
        this.monthYear = monthYear;
        this.pendingSalesCount = pendingSalesCount;
        this.pendingSalesAmount = pendingSalesAmount;
        this.pendingPurchasesCount = pendingPurchasesCount;
        this.pendingPurchasesAmount = pendingPurchasesAmount;
        this.balance = balance;
        this.profit = profit;
        this.expenses = expenses;
        this.profitPercentageChange = profitPercentageChange;
        this.expensesPercentageChange = expensesPercentageChange;
        this.balancePercentageChange = balancePercentageChange;
        this.user = user;
        this.oauthUser = oauthUser;
    }

    public BigDecimal calculateTotalTransactions() {
        BigDecimal salesAmount = pendingSalesAmount != null ? pendingSalesAmount : BigDecimal.ZERO;
        BigDecimal purchasesAmount = pendingPurchasesAmount != null ? pendingPurchasesAmount : BigDecimal.ZERO;
        return salesAmount.add(purchasesAmount);
    }
}
