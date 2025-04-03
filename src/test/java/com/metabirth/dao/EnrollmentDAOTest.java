package com.metabirth.dao;

import com.metabirth.config.JDBCConnection;
import com.metabirth.model.Enrollment;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollmentDAOTest {
    private Connection connection;
    private EnrollmentDAO enrollmentDAO;
    private static final int TEST_STUDENT_ID = 1;
    private static final int TEST_CLASS_ID = 1;
    private static final byte TEST_STATUS = 0;
    private int testEnrollmentId;

    @BeforeEach
    void setUp() {
        try {
            connection = JDBCConnection.getConnection();
            connection.setAutoCommit(false);
            enrollmentDAO = new EnrollmentDAO(connection);

            Enrollment testEnrollment = new Enrollment(0, TEST_STUDENT_ID, TEST_CLASS_ID, TEST_STATUS, LocalDateTime.now(), null, null);
            enrollmentDAO.addEnrollment(testEnrollment);

            List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();
            testEnrollmentId = enrollments.get(enrollments.size() - 1).getEnrollmentId();
        } catch (SQLException e) {
            throw new RuntimeException("테스트 데이터 준비 중 오류 발생 : " + e.getMessage());
        }
    }

    @Test
    @DisplayName("모든 수강신청 조회 테스트")
    void testGetAllEnrollments() {
        List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();

        Assertions.assertNotNull(enrollments);
        Assertions.assertFalse(enrollments.isEmpty());
    }

    @Test
    @DisplayName("특정 수강신청 조회 테스트")
    void testGetEnrollmentById() {
        Enrollment enrollment = enrollmentDAO.getEnrollmentById(testEnrollmentId);

        Assertions.assertNotNull(enrollment);
        Assertions.assertEquals(TEST_STUDENT_ID, enrollment.getStudentId());
        Assertions.assertEquals(TEST_CLASS_ID, enrollment.getClassId());
        Assertions.assertEquals(TEST_STATUS, enrollment.getStatus());
    }

    @Test
    @DisplayName("수강신청 추가 테스트")
    void testAddEnrollment() {
        Enrollment newEnrollment = new Enrollment(0, 2, 2, (byte) 0, LocalDateTime.now(), null, null);

        boolean isAdded = enrollmentDAO.addEnrollment(newEnrollment);

        Assertions.assertTrue(isAdded);
    }

    @Test
    @DisplayName("수강신청 수정 테스트")
    void testUpdateEnrollment() {
        Enrollment updatedEnrollment = new Enrollment(testEnrollmentId, 2, 2, (byte) 0, LocalDateTime.now(), null, null);

        boolean isUpdated = enrollmentDAO.updateEnrollment(updatedEnrollment);

        Assertions.assertTrue(isUpdated);

        Enrollment readEnrollment = enrollmentDAO.getEnrollmentById(testEnrollmentId);
        Assertions.assertEquals(2, readEnrollment.getStudentId());
        Assertions.assertEquals(2, readEnrollment.getClassId());
    }

    @Test
    @DisplayName("수강신청 삭제 테스트")
    void testDeleteEnrollment() {
        boolean isDeleted = enrollmentDAO.deleteEnrollment(testEnrollmentId);

        Assertions.assertTrue(isDeleted);

        Enrollment retrievedEnrollment = enrollmentDAO.getEnrollmentById(testEnrollmentId);
        Assertions.assertNull(retrievedEnrollment);
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