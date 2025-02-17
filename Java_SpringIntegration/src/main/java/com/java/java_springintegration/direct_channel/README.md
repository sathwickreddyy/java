## Implementation Plan

![Direct Channel Example]("direct_channel_example.png")


## Code

![Code Explanation]("code_explain.jpeg")


## Testing

### Request
```
POST http://localhost:8080/orders/placeOrder
Content-Type: application/json

{
"orderId": 20,
"itemName": "Biryani",
"amount": 2500,
"orderStatus": ""
}
```

### Response

```
HTTP/1.1 200
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 17 Feb 2025 19:06:13 GMT

{
"orderId": 20,
"itemName": "Biryani",
"amount": 2500.0,
"orderStatus": "Order Successfully Placed !!!"
}
Response file saved.
> 2025-02-18T003613.200.json

Response code: 200; Time: 5ms (5 ms); Content length: 97 bytes (97 B)
```

### Logs
```
2025-02-18T00:34:46.485+05:30  INFO 7766 --- [Java_SpringIntegration] [nio-8080-exec-1] c.j.j.d.service.OrderService             : Order Received, Processing Order ...
2025-02-18T00:34:46.485+05:30  INFO 7766 --- [Java_SpringIntegration] [nio-8080-exec-1] c.j.j.d.service.OrderService             : Order Processed, Placing Order ...
2025-02-18T00:34:46.486+05:30  INFO 7766 --- [Java_SpringIntegration] [nio-8080-exec-1] c.j.j.d.service.OrderService             : Responding to client that -> Order Placed Successfully !!!
```