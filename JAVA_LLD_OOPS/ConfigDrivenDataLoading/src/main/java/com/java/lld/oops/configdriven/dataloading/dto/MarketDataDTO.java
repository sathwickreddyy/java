package com.java.lld.oops.configdriven.dataloading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataDTO {
    private LocalDate tradeDate;
    private String currency;
    private BigDecimal exchangeRate;
    private Long tradingVolume;
}
