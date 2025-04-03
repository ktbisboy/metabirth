package com.metabirth.dao;

import com.metabirth.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private static final Logger log = LoggerFactory.getLogger(PaymentDAO.class);
    private final Connection connection;

    public PaymentDAO(Connection connection) {
        this.connection = connection;
    }

    // 모든 결제내역 조회
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp updatedTs = rs.getTimestamp("updated_at");
                LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                Timestamp deletedTs = rs.getTimestamp("deleted_at");
                LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                payments.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getBigDecimal("amount"),
                        rs.getByte("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        updatedAt,
                        deletedAt,
                        rs.getInt("enrollment_id")
                ));
            }
        } catch (SQLException e) {
            log.warn("[DAO] 결제 전체 조회 실패 - 사유: {}", e.getMessage());
            return null;
        }
        return payments;
    }

    // 특정 결제내역 조회
    public Payment getPaymentById(int paymentId) {
        Payment payment = null;
        String sql = "SELECT * FROM payments WHERE payment_id = ? AND status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                    Timestamp deletedTs = rs.getTimestamp("deleted_at");
                    LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                    payment = new Payment(
                            rs.getInt("payment_id"),
                            rs.getBigDecimal("amount"),
                            rs.getByte("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            updatedAt,
                            deletedAt,
                            rs.getInt("enrollment_id")
                    );
                }
            }
        } catch (SQLException e) {
            log.warn("[DAO] 결제 단건 조회 실패 (ID: {}) - 사유: {}", paymentId, e.getMessage());
            return null;
        }
        return payment;
    }

    // 결제내역 삽입
    /* - payment_id : 고유 식별자 (AUTO_INCREMENT)
     * - status : 결제 상태 (0 = 활성, 1 = 논리적 삭제)
     * - created_at : 생성 시각 (기본값 = NOW())
     * - enrollment_id : 수강신청 테이블의 외래키
     * - active_enrollment_id : 생성 컬럼 (status가 0일 때만 enrollment_id를 갖음)
     *  → 활성 상태 레코드에 대해서만 (enrollment_id) 유니크 제약 적용
     * - 논리 삭제된 후 동일 enrollment_id를 가진 결제내역을 다시 등록할 수 있음
     *  (예: 삭제된 결제 다시 등록 시 제약 조건 위반 없음)
     * */
    public boolean addPayment(Payment payment) {
        String sql = """
                INSERT INTO payments (amount, enrollment_id)
                VALUES (?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, payment.getAmount());
            ps.setInt(2, payment.getEnrollmentId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 결제 등록 실패 - enrollmentId: {}, 사유: {}", payment.getEnrollmentId(), e.getMessage());
            return false;
        }
    }

    // 결제내역 업데이트 (enrollment_id는 외래키이므로 수정 불가)
    public boolean updatePayment(Payment payment) {
        String sql = """
                UPDATE payments SET amount = ?, updated_at = now()
                WHERE payment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, payment.getAmount());
            ps.setInt(2, payment.getPaymentId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 결제 수정 실패 - paymentId: {}, 사유: {}", payment.getPaymentId(), e.getMessage());
            return false;
        }
    }

    // 결제내역 삭제 (논리적 삭제)
    public boolean deletePayment(int paymentId) {
        String sql = """
                UPDATE payments SET status = 1, updated_at = now(), deleted_at = now()
                WHERE payment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            int affectedRows = ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("[DAO] 결제 삭제 실패 - paymentId: {}, 사유: {}", paymentId, e.getMessage());
            return false;
        }
    }

    // 결제내역 삭제 (논리적 삭제), 수강신청 ID 이용
    public boolean deletePaymentByEnrollmentId(int enrollmentId) {
        String sql = """
                UPDATE payments SET status = 1, updated_at = now(), deleted_at = now()
                WHERE enrollment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            int affectedRows = ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("[DAO] 결제 삭제 실패 - enrollmentId: {}, 사유: {}", enrollmentId, e.getMessage());
            return false;
        }
    }
}