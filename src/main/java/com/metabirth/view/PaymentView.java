package com.metabirth.view;

import com.metabirth.model.Payment;
import com.metabirth.service.EnrollmentAggregateService;
import com.metabirth.service.PaymentService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class PaymentView {
    private final PaymentService paymentService;
    private final EnrollmentAggregateService integratedService;
    private final Scanner scanner;

    /*
     * 생성자
     * - 서비스 객체를 생성하여 주입받고, 콘솔 UI를 위한 Scanner 객체 초기화
     * - 데이터베이스 연결을 위한 Connection 객체
     * */
    public PaymentView(Connection connection) {
        this.paymentService = new PaymentService(connection);
        this.integratedService = new EnrollmentAggregateService(connection);
        this.scanner = new Scanner(System.in);
    }

    /**
     * 📌 사용자 메뉴 출력
     * - 사용자 CRUD 기능을 선택할 수 있도록 메뉴를 제공
     */
    public void showMenu() {
        while (true) {
            System.out.println("\n===== 결제내역 관리 시스템 =====");
            System.out.println("1. 전체 결제내역 조회");
            System.out.println("2. 결제내역 조회 (ID)");
            System.out.println("3. 결제내역 수정");
            System.out.println("4. 결제내역 삭제");
            System.out.println("0. 상위 메뉴로 돌아가기");
            System.out.print("선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 처리

            switch (choice) {
                case 1 -> getAllPayments();
                case 2 -> getPaymentById();
                case 3 -> updatePayment();
                case 4 -> deletePayment();
                case 0 -> {
                    System.out.println("상위 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 입력입니다. 다시 선택하세요.");
            }
        }
    }

    /**
     * 📌 전체 결제내역 조회
     * - `PaymentService`의 `getAllPayments()` 메서드를 호출하여 결제내역 목록을 출력
     */
    private void getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        if (payments == null || payments.isEmpty()) {
            System.out.println("❌ 등록된 결제내역이 없습니다.");
        } else {
            System.out.println("\n===== 전체 결제내역 목록 =====");
            payments.forEach(System.out::println);
        }
    }

    /**
     * 📌 특정 결제내역 조회
     * - 결제내역 ID를 사용하여 해당 결제내역의 정보를 출력
     */
    private void getPaymentById() {
        System.out.print("조회할 결제내역 ID를 입력하세요: ");
        int paymentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            System.out.println("❌ 해당 결제내역을 찾을 수 없습니다.");
        } else {
            System.out.println(payment);
        }
    }

    /**
     * 📌 결제내역 정보 수정 (UPDATE)
     * - 결제내역 ID를 입력받아 정보를 수정
     */
    private void updatePayment() {
        System.out.print("수정할 결제내역 ID를 입력하세요: ");
        int paymentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("새로운 결제금액 : ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine(); // 개행 문자 처리

        Payment payment = new Payment(paymentId, amount, (byte) 0, LocalDateTime.now(), null, null, 0);
        if (paymentService.updatePayment(payment)) {
            System.out.println("✅ 결제 수정 성공");
        } else {
            System.out.println("❌ 결제 수정 실패");
        }
    }

    /**
     * 📌 결제내역 삭제 (DELETE)
     * - 결제내역 ID를 입력받아 삭제
     * - 결제내역이 삭제되면, 연관된 수강신청과 리뷰가 모두 원자적으로 삭제된다.
     */
    private void deletePayment() {
        System.out.println("주의! 결제내역이 삭제되면 연결된 수강신청과 리뷰가 전부 삭제됩니다.");
        System.out.print("삭제할 결제내역 ID를 입력하세요: ");
        int paymentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        if (integratedService.deletePayment(paymentId)) {
            System.out.println("✅ 결제 삭제 성공");
        } else {
            System.out.println("❌ 결제 삭제 실패");
        }
    }
}