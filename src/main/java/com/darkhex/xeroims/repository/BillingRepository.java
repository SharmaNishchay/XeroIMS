package com.darkhex.xeroims.repository;

import com.darkhex.xeroims.model.Billing;
import com.darkhex.xeroims.model.OauthUser;
import com.darkhex.xeroims.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findByUserOrderByRecordDateDesc(User user);
    List<Billing> findByOauthUserOrderByRecordDateDesc(OauthUser oauthUser);

    Optional<Billing> findFirstByUserOrderByRecordDateDesc(User user);
    Optional<Billing> findFirstByOauthUserOrderByRecordDateDesc(OauthUser oauthUser);

    Optional<Billing> findTopByUserOrderByIdDesc(User user);
    Optional<Billing> findTopByOauthUserOrderByIdDesc(OauthUser oauthUser);

    List<Billing> findByMonthYearAndUser(YearMonth monthYear, User user);
    List<Billing> findByMonthYearAndOauthUser(YearMonth monthYear, OauthUser oauthUser);

    List<Billing> findByRecordDateBetweenAndUserOrderByRecordDateDesc(
            LocalDate startDate, LocalDate endDate, User user);
    List<Billing> findByRecordDateBetweenAndOauthUserOrderByRecordDateDesc(
            LocalDate startDate, LocalDate endDate, OauthUser oauthUser);

    @Query("SELECT SUM(b.balance) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.user = :user")
    BigDecimal sumBalanceByDateRangeAndUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT SUM(b.balance) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.oauthUser = :oauthUser")
    BigDecimal sumBalanceByDateRangeAndOauthUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("oauthUser") OauthUser oauthUser);

    @Query("SELECT SUM(b.profit) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.user = :user")
    BigDecimal sumProfitByDateRangeAndUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT SUM(b.profit) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.oauthUser = :oauthUser")
    BigDecimal sumProfitByDateRangeAndOauthUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("oauthUser") OauthUser oauthUser);

    @Query("SELECT SUM(b.expenses) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.user = :user")
    BigDecimal sumExpensesByDateRangeAndUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("user") User user);

    @Query("SELECT SUM(b.expenses) FROM Billing b WHERE b.recordDate BETWEEN :startDate AND :endDate AND b.oauthUser = :oauthUser")
    BigDecimal sumExpensesByDateRangeAndOauthUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("oauthUser") OauthUser oauthUser);

    @Query("SELECT DISTINCT b.monthYear FROM Billing b WHERE b.user = :user ORDER BY b.monthYear DESC")
    List<YearMonth> findDistinctMonthYearsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT b.monthYear FROM Billing b WHERE b.oauthUser = :oauthUser ORDER BY b.monthYear DESC")
    List<YearMonth> findDistinctMonthYearsByOauthUser(@Param("oauthUser") OauthUser oauthUser);
}
