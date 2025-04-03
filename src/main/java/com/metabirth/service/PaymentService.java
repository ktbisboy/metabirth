package com.metabirth.service;

import com.metabirth.dao.PaymentDAO;
import com.metabirth.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentDAO paymentDAO;
    private final Connection connection;

    // 생성자 주입(의존성 주입)
    public PaymentService(Connection connection) {
        this.connection = connection;
        this.paymentDAO = new PaymentDAO(connection);
    }

    // 모든 결제내역 조회
    public List<Payment> getAllPayments() {
        return paymentDAO.getAllPayments();
    }

    // 특정 결제내역 조회(payment_id)
    public Payment getPaymentById(int paymentId) {
        return paymentDAO.getPaymentById(paymentId);
    }

    // 결제내역 업데이트
    public boolean updatePayment(Payment payment) {
        Payment existing = paymentDAO.getPaymentById(payment.getPaymentId());
        if (existing == null) {
            log.warn("존재하지 않는 결제 ID로 업데이트 시도됨: {}", payment.getPaymentId());
            return false;
        }
        return paymentDAO.updatePayment(payment);
    }
}