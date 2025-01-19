package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.PaymentCallbackRequest;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> handlePaymentWebhook(@RequestBody PaymentCallbackRequest request) {
        log.info("Received payment webhook: orderId={}, paymentId={}",
                request.getRazorpay_order_id(), request.getRazorpay_payment_id());

        PaymentDTO payment = paymentService.processPaymentCallback(request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
    }
}
