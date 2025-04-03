package com.metabirth.service;

import com.metabirth.dao.EnrollmentDAO;
import com.metabirth.dao.PaymentDAO;
import com.metabirth.dao.ReviewDAO;
import com.metabirth.model.Enrollment;
import com.metabirth.model.Payment;
import com.metabirth.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class IntegratedService {
    private static final Logger log = LoggerFactory.getLogger(IntegratedService.class);
    private final EnrollmentDAO enrollmentDAO;
    private final PaymentDAO paymentDAO;
    private final ReviewDAO reviewDAO;
    private final Connection connection;

    public IntegratedService(Connection connection) {
        this.connection = connection;
        this.enrollmentDAO = new EnrollmentDAO(connection);
        this.paymentDAO = new PaymentDAO(connection);
        this.reviewDAO = new ReviewDAO(connection);
    }

    /**
     * 수강신청 등록 + 결제내역 등록
     * 수강신청과 결제내역을 원자적으로 등록하는 메서드
     * 또한, 결제내역 등록 시 연결된 수강신청의 활성 상태를 확인해야 한다.
     */
    public boolean registerEnrollmentAndPayment(Enrollment enrollment, Payment payment) {
        try {
            connection.setAutoCommit(false);

            // 1. 수강신청 등록
            boolean enrollmentCreated = enrollmentDAO.addEnrollment(enrollment);
            if (!enrollmentCreated) {
                connection.rollback();
                log.warn("수강신청 등록 실패. 전체 롤백 처리.");
                return false;
            }

            // 2. 등록 후 생성된 enrollmentId를 payment에 설정
            payment.setEnrollmentId(enrollment.getEnrollmentId());

            // 3. 결제 등록
            boolean paymentCreated = paymentDAO.addPayment(payment);
            if (!paymentCreated) {
                connection.rollback();
                log.warn("결제 등록 실패. 전체 롤백 처리.");
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { log.error("롤백 실패: {}", ex.getMessage()); }
            log.error("등록 트랜잭션 실패: {}", e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { log.error("AutoCommit 복원 실패: {}", e.getMessage()); }
        }
    }

    /**
     * 리뷰 등록
     * 리뷰 등록 시 연결된 수강신청의 활성 상태를 확인해야 한다.
     */
    public boolean registerReview(Review review) {
        // 수강신청 활성 상태 확인
        if (!enrollmentDAO.isActiveEnrollment(review.getEnrollmentId())) {
            log.warn("비활성화된 수강신청 ID로 리뷰 등록 시도됨: {}", review.getEnrollmentId());
            return false;
        }
        return reviewDAO.addReview(review);
    }

    /*
    * 수강신청 삭제
    * 수강신청 삭제 시, 연결되어 있는 결제내역과 리뷰 또한 삭제되어야 한다.
    * */
    public boolean deleteEnrollment(int enrollmentId) {
        Enrollment existing = enrollmentDAO.getEnrollmentById(enrollmentId);
        if (existing == null) {
            log.warn("존재하지 않는 수강신청 ID로 삭제 시도됨: {}", enrollmentId);
            return false;
        }

        try {
            connection.setAutoCommit(false);

            // 1. 결제 삭제 (연관 결제 논리 삭제)
            boolean paymentDeleted = paymentDAO.deletePaymentByEnrollmentId(enrollmentId);

            // 2. 리뷰 삭제 (연관 리뷰 논리 삭제)
            boolean reviewDeleted = reviewDAO.deleteReviewByEnrollmentId(enrollmentId);

            // 3. 수강신청 삭제
            boolean enrollmentDeleted = enrollmentDAO.deleteEnrollment(enrollmentId);

            if (!paymentDeleted || !reviewDeleted || !enrollmentDeleted) {
                connection.rollback();
                log.warn("수강신청 삭제 중 하나 이상의 삭제가 실패함. 전체 롤백 처리.");
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { log.error("롤백 실패: {}", ex.getMessage()); }
            log.error("수강신청 삭제 트랜잭션 실패: {}", e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { log.error("AutoCommit 복원 실패: {}", ex.getMessage()); }
        }
    }

    /*
     * 결제내역 삭제
     * 결제내역 삭제 시, 연결된 수강신청과 리뷰 또한 삭제되어야 한다.
     * */
    public boolean deletePayment(int paymentId) {
        Payment existing = paymentDAO.getPaymentById(paymentId);
        if (existing == null) {
            log.warn("존재하지 않는 결제 ID로 삭제 시도됨: {}", paymentId);
            return false;
        }

        int enrollmentId = existing.getEnrollmentId();
        try {
            connection.setAutoCommit(false);

            // 1. 결제 삭제
            boolean paymentDeleted = paymentDAO.deletePayment(paymentId);

            // 2. 리뷰 삭제 (해당 수강신청에 연결된 리뷰 삭제)
            boolean reviewDeleted = reviewDAO.deleteReviewByEnrollmentId(enrollmentId);

            // 3. 수강신청 삭제
            boolean enrollmentDeleted = enrollmentDAO.deleteEnrollment(enrollmentId);

            if (!paymentDeleted || !reviewDeleted || !enrollmentDeleted) {
                connection.rollback();
                log.warn("결제 삭제 중 하나 이상의 삭제가 실패함. 전체 롤백 처리.");
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { log.error("롤백 실패: {}", ex.getMessage()); }
            log.error("결제 삭제 트랜잭션 실패: {}", e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { log.error("AutoCommit 복원 실패: {}", ex.getMessage()); }
        }
    }
}
