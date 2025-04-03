package com.metabirth.dao;

import com.metabirth.config.JDBCConnection;
import com.metabirth.model.Enrollment;
import com.metabirth.model.Payment;
import com.metabirth.model.Review;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReviewDAOTest {
    private Connection connection;
    private ReviewDAO reviewDAO;
    private static final byte TEST_RATING = 1;
    private static final String TEST_CONTENT = "테스트 콘텐츠";
    private static final byte TEST_STATUS = 0;
    private int testReviewId;
    private int testEnrollmentId1;
    private int testEnrollmentId2;

    @BeforeEach
    void setUp() {
        try {
            connection = JDBCConnection.getConnection();
            connection.setAutoCommit(false);
            reviewDAO = new ReviewDAO(connection);

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

            Review testReview = new Review(0, TEST_RATING, TEST_CONTENT, TEST_STATUS, LocalDateTime.now(), null, null, testEnrollmentId1);
            reviewDAO.addReview(testReview);

            List<Review> reviews = reviewDAO.getAllReviews();
            testReviewId = reviews.get(reviews.size() - 1).getReviewId();
        } catch (SQLException e) {
            throw new RuntimeException("테스트 데이터 준비 중 오류 발생 : " + e.getMessage());
        }
    }

    @Test
    @DisplayName("모든 리뷰 조회 테스트")
    void testGetAllReviews() {
        List<Review> reviews = reviewDAO.getAllReviews();

        Assertions.assertNotNull(reviews);
        Assertions.assertFalse(reviews.isEmpty());
    }

    @Test
    @DisplayName("특정 리뷰 조회 테스트")
    void testGetReviewById() {
        Review review = reviewDAO.getReviewById(testReviewId);

        Assertions.assertNotNull(review);
        Assertions.assertEquals(TEST_RATING, review.getRating());
        Assertions.assertEquals(TEST_CONTENT, review.getContent());
        Assertions.assertEquals(TEST_STATUS, review.getStatus());
    }

    @Test
    @DisplayName("리뷰 추가 테스트")
    void testAddReview() {
        Review newReview = new Review(0, (byte) 2, "삽입 콘텐츠", (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId2);

        boolean isAdded = reviewDAO.addReview(newReview);

        Assertions.assertTrue(isAdded);
    }

    @Test
    @DisplayName("중복 리뷰 추가 테스트")
    void testAddDuplicateReview() {
        Review duplicateReview = new Review(0, (byte) 2, "중복 콘텐츠", (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId1);

        boolean isAdded = reviewDAO.addReview(duplicateReview);

        Assertions.assertFalse(isAdded);
    }

    @Test
    @DisplayName("리뷰 수정 테스트")
    void testUpdateReview() {
        Review updatedReview = new Review(testReviewId, (byte) 3, "수정 콘텐츠", (byte) 0, LocalDateTime.now(), null, null, testEnrollmentId1);

        boolean isUpdated = reviewDAO.updateReview(updatedReview);

        Assertions.assertTrue(isUpdated);

        Review readReview = reviewDAO.getReviewById(testReviewId);
        Assertions.assertEquals((byte) 3, readReview.getRating());
        Assertions.assertEquals("수정 콘텐츠", readReview.getContent());
    }

    @Test
    @DisplayName("리뷰 삭제 (review_id 기준) 테스트")
    void testDeleteReview() {
        boolean isDeleted = reviewDAO.deleteReview(testReviewId);

        Assertions.assertTrue(isDeleted);

        Review retrievedReview = reviewDAO.getReviewById(testReviewId);
        Assertions.assertNull(retrievedReview);
    }

    @Test
    @DisplayName("리뷰 삭제 (수강신청 ID 기준) 테스트")
    void testDeleteReviewByEnrollmentId() {
        boolean isDeleted = reviewDAO.deleteReviewByEnrollmentId(testEnrollmentId1);

        Assertions.assertTrue(isDeleted);

        Review retrievedReview = reviewDAO.getReviewById(testReviewId);
        Assertions.assertNull(retrievedReview);
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