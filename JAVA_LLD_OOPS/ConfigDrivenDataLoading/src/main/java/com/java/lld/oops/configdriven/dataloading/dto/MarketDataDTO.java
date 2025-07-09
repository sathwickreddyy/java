package com.java.lld.oops.configdriven.dataloading.dto;

import javax.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Market Data DTO with comprehensive validation and data quality checks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MarketDataDTO {

    @NotNull(message = "Trade date is required")
    @PastOrPresent(message = "Trade date cannot be in the future")
    private LocalDate tradeDate;

    @NotBlank(message = "Currency pair cannot be blank")
    @Size(min = 6, max = 7, message = "Currency pair must be 6-7 characters (e.g., USD/EUR)")
    @Pattern(regexp = "^[A-Z]{3}/[A-Z]{3}$", message = "Currency pair must follow format XXX/YYY")
    private String currency;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be positive")
    @Digits(integer = 10, fraction = 4, message = "Exchange rate format invalid")
    private BigDecimal exchangeRate;

    @Min(value = 0, message = "Trading volume cannot be negative")
    private Long tradingVolume;

    // Optional fields for testing scenarios
    @Size(max = 50, message = "Market type cannot exceed 50 characters")
    private String marketType;

    @Size(max = 100, message = "Region cannot exceed 100 characters")
    private String region;

    @DecimalMin(value = "0.0", message = "High price must be non-negative")
    private BigDecimal highPrice;

    @DecimalMin(value = "0.0", message = "Low price must be non-negative")
    private BigDecimal lowPrice;

    private LocalDateTime lastUpdated;

    private Boolean isActive;

    // Custom validation method
    @AssertTrue(message = "High price must be greater than or equal to low price")
    private boolean isHighPriceValid() {
        if (highPrice == null || lowPrice == null) {
            return true; // Skip validation if either is null
        }
        return highPrice.compareTo(lowPrice) >= 0;
    }
}
