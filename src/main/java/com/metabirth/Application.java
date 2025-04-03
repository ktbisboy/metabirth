package com.metabirth;

import com.metabirth.config.JDBCConnection;
import com.metabirth.view.EnrollmentView;
import com.metabirth.view.PaymentView;
import com.metabirth.view.ReviewView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws SQLException {
        Connection connection = JDBCConnection.getConnection();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== 아카데미 관리자 시스템 =====");
            System.out.println("1. 수강신청(Enrollment) 관리");
            System.out.println("2. 결제내역(Payment) 관리");
            System.out.println("3. 리뷰(Review) 관리");
            System.out.println("0. 종료");
            System.out.print("선택: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 처리

            switch (choice) {
                case 1 -> startEnrollmentManagement(connection);
                case 2 -> startPaymentManagement(connection);
                case 3 -> startReviewManagement(connection);
                case 0 -> {
                    connection.close();
                    System.out.println("🚀 프로그램을 종료합니다.");
                    return;
                }
                default -> System.out.println("❌ 잘못된 입력입니다. 다시 선택하세요.");
            }
        }
    }

    /**
     * 📌 수강신청(Enrollment) 관리 시작
     * - 수강신청(Enrollment) 관련 기능 실행
     */
    private static void startEnrollmentManagement(Connection connection) {
        EnrollmentView enrollmentView = new EnrollmentView(connection);
        enrollmentView.showMenu();
    }

    /**
     * 📌 결제내역(Payment) 관리 시작
     * - 결제내역(Payment) 관련 기능 실행
     */
    private static void startPaymentManagement(Connection connection) {
        PaymentView paymentView = new PaymentView(connection);
        paymentView.showMenu();
    }

    /**
     * 📌 리뷰(Review) 관리 시작
     * - 리뷰(Review) 관련 기능 실행
     */
    private static void startReviewManagement(Connection connection) {
        ReviewView reviewView = new ReviewView(connection);
        reviewView.showMenu();
    }

}