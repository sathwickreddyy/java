package com.java.java_springintegration.direct_channel.model;

import lombok.Data;

@Data
public class Order {
    private int orderId;
    private String itemName;
    private Double amount;
    private String orderStatus;
}
