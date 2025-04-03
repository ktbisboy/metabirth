package com.metabirth.service;

import com.metabirth.dao.ReviewDAO;
import com.metabirth.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class ReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private final ReviewDAO reviewDAO;
    private final Connection connection;

    // 생성자 주입(의존성 주입)
    public ReviewService(Connection connection) {
        this.connection = connection;
        this.reviewDAO = new ReviewDAO(connection);
    }

    // 모든 리뷰 조회
    public List<Review> getAllReviews() {
        return reviewDAO.getAllReviews();
    }

    // 특정 리뷰 조회
    public Review getReviewById(int reviewId) {
        return reviewDAO.getReviewById(reviewId);
    }

    // 리뷰 업데이트
    public boolean updateReview(Review review) {
        Review existing = reviewDAO.getReviewById(review.getReviewId());
        if (existing == null) {
            log.warn("존재하지 않는 리뷰 ID로 업데이트 시도됨: {}", review.getReviewId());
            return false;
        }
        return reviewDAO.updateReview(review);
    }

    // 리뷰 삭제
    public boolean deleteReview(int reviewId) {
        Review existing = reviewDAO.getReviewById(reviewId);
        if (existing == null) {
            log.warn("존재하지 않는 리뷰 ID로 삭제 시도됨: {}", reviewId);
            return false;
        }
        return reviewDAO.deleteReview(reviewId);
    }
}