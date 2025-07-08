package com.java.lld.oops.configdriven.dataloading.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Portfolio DTO with nested validation scenarios and business rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PortfolioDTO {

    @NotBlank(message = "Portfolio ID is required")
    @Pattern(regexp = "^PORT\\d{3}$", message = "Portfolio ID must follow pattern PORT### (e.g., PORT001)")
    private String portfolioId;

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 3, max = 100, message = "Portfolio name must be between 3 and 100 characters")
    private String portfolioName;

    @NotNull(message = "Risk profile is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|ULTRA_HIGH)$", message = "Risk profile must be LOW, MEDIUM, HIGH, or ULTRA_HIGH")
    private String riskProfile;

    @NotNull(message = "Liquidity threshold is required")
    @DecimalMin(value = "0.0", message = "Liquidity threshold must be non-negative")
    @DecimalMax(value = "1.0", message = "Liquidity threshold cannot exceed 1.0")
    private BigDecimal liquidityThreshold;

    // Optional fields for testing
    @Size(max = 100, message = "Manager name cannot exceed 100 characters")
    private String managerName;

    @Past(message = "Inception date must be in the past")
    private LocalDate inceptionDate;

    @DecimalMin(value = "0.0", message = "Total value must be non-negative")
    private BigDecimal totalValue;

    @Min(value = 0, message = "Number of holdings cannot be negative")
    @Max(value = 10000, message = "Number of holdings cannot exceed 10000")
    private Integer numberOfHoldings;

    private Boolean isActive;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter ISO code")
    private String currency;

    // Business rule validation
    @AssertTrue(message = "Ultra high risk portfolios must have liquidity threshold >= 0.8")
    private boolean isUltraHighRiskValid() {
        if (!"ULTRA_HIGH".equals(riskProfile) || liquidityThreshold == null) {
            return true;
        }
        return liquidityThreshold.compareTo(new BigDecimal("0.8")) >= 0;
    }
}
