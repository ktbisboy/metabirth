package com.metabirth.view;

import com.metabirth.model.Review;
import com.metabirth.service.IntegratedService;
import com.metabirth.service.ReviewService;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ReviewView {
    private final ReviewService reviewService;
    private final IntegratedService integratedService;
    private final Scanner scanner;

    /*
     * 생성자
     * - 서비스 객체를 생성하여 주입받고, 콘솔 UI를 위한 Scanner 객체 초기화
     * - 데이터베이스 연결을 위한 Connection 객체
     * */
    public ReviewView(Connection connection) {
        this.reviewService = new ReviewService(connection);
        this.integratedService = new IntegratedService(connection);
        this.scanner = new Scanner(System.in);
    }

    /**
     * 📌 사용자 메뉴 출력
     * - 사용자 CRUD 기능을 선택할 수 있도록 메뉴를 제공
     */
    public void showMenu() {
        while (true) {
            System.out.println("\n===== 리뷰 관리 시스템 =====");
            System.out.println("1. 전체 리뷰 조회");
            System.out.println("2. 리뷰 등록");
            System.out.println("3. 리뷰 조회 (ID)");
            System.out.println("4. 리뷰 수정");
            System.out.println("5. 리뷰 삭제");
            System.out.println("0. 상위메뉴로 돌아가기");
            System.out.print("선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 처리

            switch (choice) {
                case 1 -> getAllReviews();
                case 2 -> registerReview();
                case 3 -> getReviewById();
                case 4 -> updateReview();
                case 5 -> deleteReview();
                case 0 -> {
                    System.out.println("상위 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 입력입니다. 다시 선택하세요.");
            }
        }
    }

    /**
     * 📌 전체 리뷰 조회
     * - `ReviewService`의 `getAllReviews()` 메서드를 호출하여 리뷰 목록을 출력
     */
    private void getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        if (reviews == null || reviews.isEmpty()) {
            System.out.println("❌ 등록된 리뷰가 없습니다.");
        } else {
            System.out.println("\n===== 전체 리뷰 목록 =====");
            reviews.forEach(System.out::println);
        }
    }

    /**
     * 📌 특정 리뷰 조회
     * - 리뷰 ID를 입력받아 해당 리뷰의 정보를 출력
     */
    private void getReviewById() {
        System.out.print("조회할 리뷰 ID를 입력하세요: ");
        int reviewId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            System.out.println("❌ 해당 리뷰를 찾을 수 없습니다.");
        } else {
            System.out.println("\n===== 리뷰 정보 =====");
            System.out.println(review);
        }
    }


    /**
     * 📌 리뷰 등록 (CREATE)
     * - 리뷰 정보를 입력받아 새로운 리뷰를 등록
     * - 결제내역 등록과정에서 연관된 수강신청이 활성화 상태인지 확인되어야 함.
     * - -> 복합 작업은 IntegratedService에서 수행됨.
     */
    private void registerReview() {
        System.out.print("평점(1~5): ");
        byte rating = scanner.nextByte();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("리뷰 내용: ");
        String content = scanner.nextLine();

        System.out.print("수강신청 ID: ");
        int enrollmentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        Review review = new Review(0, rating, content, (byte) 0, LocalDateTime.now(), null, null, enrollmentId);
        boolean success = integratedService.registerReview(review);
        if (success) {
            System.out.println("✅ 리뷰 등록 성공");
        } else {
            System.out.println("❌ 리뷰 등록 실패");
        }
    }

    /**
     * 📌 리뷰 정보 수정 (UPDATE)
     * - 리뷰 ID를 입력받아 정보를 수정
     */
    private void updateReview() {
        System.out.print("수정할 리뷰 ID를 입력하세요: ");
        int reviewId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("새로운 평점(1~5): ");
        byte rating = scanner.nextByte();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("새로운 리뷰 내용: ");
        String content = scanner.nextLine();

        Review review = new Review(reviewId, rating, content, (byte) 0, LocalDateTime.now(), null, null, 0);
        boolean success = reviewService.updateReview(review);
        if (success) {
            System.out.println("✅ 리뷰 수정 성공");
        } else {
            System.out.println("❌ 리뷰 수정 실패");
        }
    }

    /**
     * 📌 리뷰 삭제 (DELETE)
     * - 리뷰 ID를 입력받아 삭제
     */
    private void deleteReview() {
        System.out.print("삭제할 리뷰 ID를 입력하세요: ");
        int reviewId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        boolean success = reviewService.deleteReview(reviewId);
        if (success) {
            System.out.println("✅ 리뷰 삭제 성공");
        } else {
            System.out.println("❌ 리뷰 삭제 실패");
        }
    }
}