package com.metabirth.view;

import com.metabirth.model.Enrollment;
import com.metabirth.model.Payment;
import com.metabirth.service.EnrollmentService;
import com.metabirth.service.EnrollmentAggregateService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class EnrollmentView {
    private final EnrollmentService enrollmentService;
    private final EnrollmentAggregateService integratedService;
    private final Scanner scanner;

    /*
     * 생성자
     * - 서비스 객체를 생성하여 주입받고, 콘솔 UI를 위한 Scanner 객체 초기화
     * - 데이터베이스 연결을 위한 Connection 객체
     * */
    public EnrollmentView(Connection connection) {
        this.enrollmentService = new EnrollmentService(connection);
        this.integratedService = new EnrollmentAggregateService(connection);
        this.scanner = new Scanner(System.in);
    }

    /**
     * 📌 사용자 메뉴 출력
     * - 사용자 CRUD 기능을 선택할 수 있도록 메뉴를 제공
     */
    public void showMenu() {
        while (true) {
            System.out.println("1. 전체 수강신청 조회");
            System.out.println("2. 수강신청 등록");
            System.out.println("3. 수강신청 조회 (ID)");
            System.out.println("4. 수강신청 수정");
            System.out.println("5. 수강신청 삭제");
            System.out.println("0. 상위 메뉴로 돌아가기");
            System.out.print("선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 처리

            switch (choice) {
                case 1 -> getAllEnrollments();
                case 2 -> registerEnrollmentAndPayment();
                case 3 -> getEnrollmentById();
                case 4 -> updateEnrollment();
                case 5 -> deleteEnrollment();
                case 0 -> {
                    System.out.println("상위 메뉴로 돌아갑니다.");
                    return;
                }
                default -> System.out.println("잘못된 입력입니다. 다시 선택하세요.");
            }
        }
    }

    /**
     * 📌 전체 수강신청 조회
     * - `EnrollmentService`의 `getAllEnrollments()` 메서드를 호출하여 수강신청 목록을 출력
     */
    private void getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        if (enrollments == null || enrollments.isEmpty()) {
            System.out.println("❌ 등록된 수강신청이 없습니다.");
        } else {
            System.out.println("\n===== 전체 수강신청 목록 =====");
            enrollments.forEach(System.out::println);
        }
    }

    /**
     * 📌 특정 수강신청 조회
     * - 수강신청 ID를 입력받아 해당 수강신청의 정보를 출력
     */
    private void getEnrollmentById() {
        System.out.print("조회할 수강신청 ID를 입력하세요: ");
        int enrollmentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        if (enrollment == null) {
            System.out.println("❌ 해당 수강신청을 찾을 수 없습니다.");
        } else {
            System.out.println("\n===== 수강신청 정보 =====");
            System.out.println(enrollment);
        }
    }

    /**
     * 📌 수강신청 등록 + 결제내역 등록 (CREATE)
     * - 수강신청 정보와 결제내역 정보를 입력받아 새로운 수강신청와 결제내역 등록
     * - 둘은 원자적으로 삽입되어야 하는 관계
     * - -> 복합 작업은 IntegratedService에서 수행됨.
     */
    private void registerEnrollmentAndPayment() {
        System.out.print("학생 ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("수업 ID: ");
        int classId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        // 수강신청 정보 생성
        Enrollment enrollment = new Enrollment(0, studentId, classId, (byte) 0, LocalDateTime.now(), null, null);

        System.out.println("수강신청과 결제는 동시에 진행되어야 합니다.");
        System.out.println("해당 수강신청에 대한 결제를 진행합니다.");
        System.out.print("결제 금액: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine(); // 개행 문자 처리

        // 결제 정보 생성 (enrollmentId는 나중에 통합서비스에서 세팅됨)
        Payment payment = new Payment(0, amount, (byte) 0, LocalDateTime.now(), null, null, 0);

        // 통합 서비스의 메서드를 호출하여 원자적으로 처리
        boolean success = integratedService.registerEnrollmentAndPayment(enrollment, payment);
        if (success) {
            System.out.println("✅ 수강신청 및 결제 등록 성공");
        } else {
            System.out.println("❌ 수강신청 및 결제 등록에 실패");
        }
    }

    /**
     * 📌 수강신청 정보 수정 (UPDATE)
     * - 수강신청 ID를 입력받아 정보를 수정
     */
    private void updateEnrollment() {
        System.out.print("수정할 수강신청 ID를 입력하세요: ");
        int enrollmentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("새로운 학생 ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        System.out.print("새로운 수업 ID: ");
        int classId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        Enrollment enrollment = new Enrollment(enrollmentId, studentId, classId, (byte) 0, LocalDateTime.now(), null, null);
        boolean success = enrollmentService.updateEnrollment(enrollment);
        if (success) {
            System.out.println("✅ 수강신청 수정 성공");
        } else {
            System.out.println("❌ 수강신청 수정 실패");
        }
    }

    /**
     * 📌 수강신청 삭제 (DELETE)
     * - 수강신청 ID를 입력받아 삭제
     * - 수강신청이 삭제되면, 연관된 결제내역과 리뷰가 모두 원자적으로 삭제된다.
     */
    private void deleteEnrollment() {
        System.out.println("주의! 수강신청이 삭제되면 연결된 결제내역과 리뷰가 전부 삭제됩니다.");
        System.out.print("삭제할 수강신청 ID를 입력하세요: ");
        int enrollmentId = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 처리

        boolean success = integratedService.deleteEnrollment(enrollmentId);
        if (success) {
            System.out.println("✅ 수강신청 삭제 성공");
        } else {
            System.out.println("❌ 수강신청 삭제 실패");
        }
    }
}