package com.ecommerce.service;

import com.ecommerce.dto.PaymentCallbackRequest;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.PaymentException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${payment.mock.enabled:false}")
    private boolean mockPaymentEnabled;

    @Transactional
    public PaymentDTO createPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status. Current status: " + order.getStatus());
        }

        // Check if payment already exists
        if (order.getPayment() != null && order.getPayment().getRazorpayOrderId() != null) {
            return mapToDTO(order.getPayment());
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("INR");

        if (mockPaymentEnabled) {
            // Mock payment mode
            String mockOrderId = "mock_order_" + System.currentTimeMillis();
            payment.setRazorpayOrderId(mockOrderId);
            payment.setStatus(Payment.PaymentStatus.CREATED);
            Payment savedPayment = paymentRepository.save(payment);

            order.setStatus(Order.OrderStatus.PAYMENT_PENDING);
            orderRepository.save(order);

            // Trigger mock webhook after 3 seconds
            triggerMockWebhook(mockOrderId);

            return mapToDTO(savedPayment);
        } else {
            // Razorpay integration
            try {
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", order.getTotalAmount().multiply(new java.math.BigDecimal(100)).intValue());
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "order_" + orderId);

                com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);
                String razorpayOrderId = razorpayOrder.get("id");

                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setStatus(Payment.PaymentStatus.CREATED);

                Payment savedPayment = paymentRepository.save(payment);

                order.setStatus(Order.OrderStatus.PAYMENT_PENDING);
                orderRepository.save(order);

                return mapToDTO(savedPayment);
            } catch (RazorpayException e) {
                log.error("Razorpay order creation failed: {}", e.getMessage());
                throw new PaymentException("Failed to create Razorpay order: " + e.getMessage(), e);
            }
        }
    }

    @Transactional
    public PaymentDTO processPaymentCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpay_order_id())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "razorpayOrderId",
                        request.getRazorpay_order_id()));

        if (mockPaymentEnabled) {
            // Mock payment verification - always succeeds
            payment.setRazorpayPaymentId(request.getRazorpay_payment_id());
            payment.setRazorpaySignature(request.getRazorpay_signature());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentMethod("mock");

            Order order = payment.getOrder();
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Mock payment successful for order: {}", order.getId());
            return mapToDTO(savedPayment);
        } else {
            // Verify Razorpay signature
            try {
                String generatedSignature = request.getRazorpay_order_id() + "|" + request.getRazorpay_payment_id();
                boolean isValid = Utils.verifySignature(generatedSignature, request.getRazorpay_signature(),
                        razorpayKeySecret);

                if (isValid) {
                    payment.setRazorpayPaymentId(request.getRazorpay_payment_id());
                    payment.setRazorpaySignature(request.getRazorpay_signature());
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setPaymentMethod("razorpay");

                    Order order = payment.getOrder();
                    order.setStatus(Order.OrderStatus.PAID);
                    orderRepository.save(order);

                    Payment savedPayment = paymentRepository.save(payment);
                    log.info("Payment successful for order: {}", order.getId());
                    return mapToDTO(savedPayment);
                } else {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);

                    Order order = payment.getOrder();
                    order.setStatus(Order.OrderStatus.PENDING);
                    orderRepository.save(order);

                    log.error("Payment signature verification failed for order: {}", payment.getOrder().getId());
                    throw new PaymentException("Payment verification failed. Invalid signature.");
                }
            } catch (RazorpayException e) {
                log.error("Razorpay signature verification error: {}", e.getMessage());
                throw new PaymentException("Payment verification error: " + e.getMessage(), e);
            }
        }
    }

    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToDTO(payment);
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    @Async
    protected void triggerMockWebhook(String mockOrderId) {
        try {
            // Simulate 3-second delay before webhook callback
            Thread.sleep(3000);

            Payment payment = paymentRepository.findByRazorpayOrderId(mockOrderId).orElse(null);
            if (payment != null && payment.getStatus() == Payment.PaymentStatus.CREATED) {
                PaymentCallbackRequest mockCallback = new PaymentCallbackRequest();
                mockCallback.setRazorpay_order_id(mockOrderId);
                mockCallback.setRazorpay_payment_id("mock_payment_" + System.currentTimeMillis());
                mockCallback.setRazorpay_signature("mock_signature_" + System.currentTimeMillis());

                processPaymentCallback(mockCallback);
                log.info("Mock webhook triggered for order: {}", mockOrderId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Mock webhook thread interrupted", e);
        }
    }

    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
