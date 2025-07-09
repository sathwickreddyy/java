package com.java.lld.oops.configdriven.dataloading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order details with complex nested JSON mapping support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OrderDetailsDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Customer name is required")
    @Size(min = 1, max = 100, message = "Customer name must be between 1 and 100 characters")
    private String customerName;

    @Email(message = "Customer email must be valid")
    private String customerEmail;

    @Size(max = 200, message = "Primary product name cannot exceed 200 characters")
    private String primaryProduct;

    @Min(value = 1, message = "Primary quantity must be at least 1")
    private Integer primaryQuantity;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Total amount format invalid")
    private BigDecimal totalAmount;

    @Size(max = 100, message = "Shipping city cannot exceed 100 characters")
    private String shippingCity;

    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Shipping zip must be valid US zip code")
    private String shippingZip;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdDate;

    @Pattern(regexp = "^(pending|confirmed|processing|shipped|delivered|cancelled)$",
            message = "Status must be valid order status")
    private String orderStatus;

    // Additional fields for extended order information
    private String customerId;
    private String customerTier;
    private String shippingMethod;
    private String shippingState;
    private String shippingCountry;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private Integer totalItems;
}
