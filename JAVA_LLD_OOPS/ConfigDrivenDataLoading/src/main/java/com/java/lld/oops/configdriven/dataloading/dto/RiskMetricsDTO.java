package com.java.lld.oops.configdriven.dataloading.dto;

import javax.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Risk Metrics DTO with financial data validation and business rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RiskMetricsDTO {

    @NotBlank(message = "Portfolio ID is required")
    @Pattern(regexp = "^PORT\\d{3}$", message = "Portfolio ID must follow pattern PORT###")
    private String portfolioId;

    @NotNull(message = "VaR 95% is required")
    @DecimalMax(value = "0.0", message = "VaR should be negative or zero")
    private BigDecimal var95;

    @DecimalMax(value = "0.0", message = "Expected Shortfall should be negative or zero")
    private BigDecimal expectedShortfall;

    @DecimalMin(value = "0.0", message = "Liquidity score must be non-negative")
    @DecimalMax(value = "100.0", message = "Liquidity score cannot exceed 100")
    private BigDecimal liquidityScore;

    @NotNull(message = "Report date is required")
    @PastOrPresent(message = "Report date cannot be in the future")
    private LocalDate reportDate;

    // Additional risk metrics for testing
    @DecimalMin(value = "-5.0", message = "Beta cannot be less than -5.0")
    @DecimalMax(value = "5.0", message = "Beta cannot exceed 5.0")
    private BigDecimal beta;

    @DecimalMin(value = "-10.0", message = "Sharpe ratio cannot be less than -10.0")
    @DecimalMax(value = "10.0", message = "Sharpe ratio cannot exceed 10.0")
    private BigDecimal sharpeRatio;

    @DecimalMax(value = "0.0", message = "Max drawdown should be negative or zero")
    private BigDecimal maxDrawdown;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|EXTREME)$", message = "Risk category must be LOW, MEDIUM, HIGH, or EXTREME")
    private String riskCategory;

    private Boolean isStressTestPassed;

    // Business rule: Expected Shortfall should be worse (more negative) than VaR
    @AssertTrue(message = "Expected Shortfall should be worse (more negative) than VaR 95%")
    private boolean isExpectedShortfallValid() {
        if (var95 == null || expectedShortfall == null) {
            return true;
        }
        return expectedShortfall.compareTo(var95) <= 0;
    }
}
