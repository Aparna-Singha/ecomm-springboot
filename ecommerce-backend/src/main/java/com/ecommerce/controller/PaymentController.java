package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CreatePaymentRequest;
import com.ecommerce.dto.PaymentCallbackRequest;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentDTO payment = paymentService.createPayment(request.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/razorpay-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getRazorpayKey() {
        Map<String, String> response = new HashMap<>();
        response.put("key", paymentService.getRazorpayKeyId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
