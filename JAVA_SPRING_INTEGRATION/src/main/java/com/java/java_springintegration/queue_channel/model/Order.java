package com.java.java_springintegration.queue_channel.model;

import lombok.Data;

@Data
public class Order {
    private String orderId;
    private String itemName;
    private Double amount;
    private String orderStatus;

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", amount=" + amount +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}
