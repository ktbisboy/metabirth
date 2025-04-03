package com.metabirth.dao;

import com.metabirth.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    private static final Logger log = LoggerFactory.getLogger(ReviewDAO.class);
    private final Connection connection;

    public ReviewDAO(Connection connection) {
        this.connection = connection;
    }

    // 모든 리뷰 조회
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp updatedTs = rs.getTimestamp("updated_at");
                LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                Timestamp deletedTs = rs.getTimestamp("deleted_at");
                LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                reviews.add(new Review(
                        rs.getInt("review_id"),
                        rs.getByte("rating"),
                        rs.getString("content"),
                        rs.getByte("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        updatedAt,
                        deletedAt,
                        rs.getInt("enrollment_id")
                ));
            }
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 전체 조회 실패 - 사유: {}", e.getMessage());
            return null;
        }
        return reviews;
    }

    // 특정 리뷰 조회
    public Review getReviewById(int reviewId) {
        Review review = null;
        String sql = "SELECT * FROM reviews WHERE review_id = ? AND status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                    Timestamp deletedTs = rs.getTimestamp("deleted_at");
                    LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                    review = new Review(
                            rs.getInt("review_id"),
                            rs.getByte("rating"),
                            rs.getString("content"),
                            rs.getByte("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            updatedAt,
                            deletedAt,
                            rs.getInt("enrollment_id")
                    );
                }
            }
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 조회 실패 (ID: {}) - 사유: {}", reviewId, e.getMessage());
            return null;
        }
        return review;
    }

    // 리뷰 삽입
    /* - review_id : 고유 식별자 (AUTO_INCREMENT)
     * - status : 결제 상태 (0 = 활성, 1 = 논리적 삭제)
     * - created_at : 생성 시각 (기본값 = NOW())
     * - enrollment_id : 수강신청 테이블의 외래키
     * - active_enrollment_id : 생성 컬럼 (status가 0일 때만 enrollment_id를 갖음)
     *  → 활성 상태 레코드에 대해서만 (enrollment_id) 유니크 제약 적용
     * - 논리 삭제된 후 동일 enrollment_id를 가진 리뷰를 다시 등록할 수 있음
     *  (예: 삭제된 리뷰 다시 등록 시 제약 조건 위반 없음)
     * */
    public boolean addReview(Review review) {
        String sql = """
                INSERT INTO reviews (rating, content, enrollment_id)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setByte(1, review.getRating());
            ps.setString(2, review.getContent());
            ps.setInt(3, review.getEnrollmentId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 삽입 실패 - enrollmentId: {}, 사유: {}", review.getEnrollmentId(), e.getMessage());
            return false;
        }
    }

    // 리뷰 업데이트 (enrollment_id는 외래키이므로 수정 불가)
    public boolean updateReview(Review review) {
        String sql = """
                UPDATE reviews SET rating = ?, content = ?, updated_at = now()
                WHERE review_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setByte(1, review.getRating());
            ps.setString(2, review.getContent());
            ps.setInt(3, review.getReviewId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 업데이트 실패 - reviewId: {}, 사유: {}", review.getReviewId(), e.getMessage());
            return false;
        }
    }

    // 리뷰 삭제 (논리적 삭제)
    public boolean deleteReview(int reviewId) {
        String sql = """
                UPDATE reviews SET status = 1, updated_at = now(), deleted_at = now()
                WHERE review_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            int affectedRows = ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 삭제 실패 - reviewId: {}, 사유: {}", reviewId, e.getMessage());
            return false;
        }
    }

    // 리뷰 삭제 (논리적 삭제), 수강신청 ID 이용
    public boolean deleteReviewByEnrollmentId(int enrollmentId) {
        String sql = """
                UPDATE reviews SET status = 1, updated_at = now(), deleted_at = now()
                WHERE enrollment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            int affectedRows = ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("[DAO] 리뷰 삭제 실패 - enrollmentId: {}, 사유: {}", enrollmentId, e.getMessage());
            return false;
        }
    }
}