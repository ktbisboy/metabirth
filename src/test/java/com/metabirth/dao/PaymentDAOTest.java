package com.metabirth.dao;

import com.metabirth.config.JDBCConnection;
import com.metabirth.model.Enrollment;
import com.metabirth.model.Payment;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentDAOTest {
    private Connection connection;
    private PaymentDAO paymentDAO;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100.00");
    private static final byte TEST_STATUS = 0;
    private int testPaymentId;
    private int testEnrollmentId1;
    private int testEnrollmentId2;

    @BeforeEach
    void setUp() {
        try {
            connection = JDBCConnection.getConnection();
            connection.setAutoCommit(false);
            paymentDAO = new PaymentDAO(connection);

            // 테스트용 enrollment 레코드를 2개 추가 (제약조건)
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO(connection);
            Enrollment testEnrollment1 = new Enrollment(0, 1, 1, (byte)0, LocalDateTime.now(), null, null);
            Enrollment testEnrollment2 = new Enrollment(0, 2, 2, (byte)0, LocalDateTime.now(), null, null);
            enrollmentDAO.addEnrollment(testEnrollment1);
            enrollmentDAO.addEnrollment(testEnrollment2);

            // 방금 삽입한 enrollment들의 id를 가져옴.
            List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();
            testEnrollmentId1 = enrollments.get(enrollments.size() - 2).getEnrollmentId();
            testEnrollmentId2 = enrollments.get(enrollments.size() - 1).getEnrollmentId();

            Payment testPayment = new Payment(0, TEST_AMOUNT, TEST_STATUS, LocalDateTime.now(), null, null, testEnrollmentId1);
            paymentDAO.addPayment(testPayment);

            List<Payment> payments = paymentDAO.getAllPayments();
            testPaymentId = payments.get(payments.size() - 1).getPaymentId();
        } catch (SQLException e) {
            throw new RuntimeException("테스트 데이터 준비 중 오류 발생 : " + e.getMessage());
        }
    }

    @Test
    @DisplayName("모든 결제내역 조회 테스트")
    void testGetAllPayments() {
        List<Payment> payments = paymentDAO.getAllPayments();

        Assertions.assertNotNull(payments);
        Assertions.assertFalse(payments.isEmpty());
    }

    @Test
    @DisplayName("특정 결제내역 조회 테스트")
    void testGetPaymentById() {
        Payment payment = paymentDAO.getPaymentById(testPaymentId);

        Assertions.assertNotNull(payment);
        Assertions.assertEquals(TEST_AMOUNT, payment.getAmount());
        Assertions.assertEquals(TEST_STATUS, payment.getStatus());
    }

    @Test
    @DisplayName("결제내역 추가 테스트")
    void testAddPayment() {
        Payment newPayment = new Payment(0, new BigDecimal("200.00"), (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId2);

        boolean isAdded = paymentDAO.addPayment(newPayment);

        Assertions.assertTrue(isAdded);
    }

    @Test
    @DisplayName("중복 결제내역 추가 테스트")
    void testAddDuplicatePayment() {
        Payment duplicatePayment = new Payment(0, new BigDecimal("150.00"), (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId1);

        boolean isAdded = paymentDAO.addPayment(duplicatePayment);

        Assertions.assertFalse(isAdded);
    }

    @Test
    @DisplayName("결제내역 수정 테스트")
    void testUpdatePayment() {
        Payment updatedPayment = new Payment(testPaymentId, new BigDecimal("300.00"), (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId1);

        boolean isUpdated = paymentDAO.updatePayment(updatedPayment);

        Assertions.assertTrue(isUpdated);

        Payment readPayment = paymentDAO.getPaymentById(testPaymentId);
        Assertions.assertEquals(new BigDecimal("300.00"), readPayment.getAmount());
    }

    @Test
    @DisplayName("결제내역 삭제 (payment_id 기준) 테스트")
    void testDeletePayment() {
        boolean isDeleted = paymentDAO.deletePayment(testPaymentId);

        Assertions.assertTrue(isDeleted);

        Payment retrievedPayment = paymentDAO.getPaymentById(testPaymentId);
        Assertions.assertNull(retrievedPayment);
    }

    @Test
    @DisplayName("결제내역 삭제 (수강신청 ID 기준) 테스트")
    void testDeletePaymentByEnrollmentId() {
        boolean isDeleted = paymentDAO.deletePaymentByEnrollmentId(testEnrollmentId1);

        Assertions.assertTrue(isDeleted);

        Payment retrievedPayment = paymentDAO.getPaymentById(testPaymentId);
        Assertions.assertNull(retrievedPayment);
    }

    @AfterEach
    void tearDown() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
            System.out.println("테스트 완료 : 데이터 롤백 성공");
        } catch (SQLException e) {
            System.out.println("테스트 종류 중 오류 발생 : " + e.getMessage());
        }
    }
}