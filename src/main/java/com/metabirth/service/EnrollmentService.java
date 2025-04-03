package com.metabirth.service;

import com.metabirth.dao.EnrollmentDAO;
import com.metabirth.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class EnrollmentService {
    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);
    private final EnrollmentDAO enrollmentDAO;
    private final Connection connection;

    // 생성자 주입(의존성 주입)
    public EnrollmentService(Connection connection) {
        this.connection = connection;
        this.enrollmentDAO = new EnrollmentDAO(connection);
    }

    // 모든 수강신청 조회
    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.getAllEnrollments();
    }

    // 특정 수강신청 조회
    public Enrollment getEnrollmentById(int enrollmentId) {
        return enrollmentDAO.getEnrollmentById(enrollmentId);
    }

    // 수강신청 업데이트
    public boolean updateEnrollment(Enrollment enrollment) {
        Enrollment existing = enrollmentDAO.getEnrollmentById(enrollment.getEnrollmentId());
        if (existing == null) {
            log.warn("존재하지 않는 수강신청 ID로 업데이트 시도됨: {}", enrollment.getEnrollmentId());
            return false;
        }
        return enrollmentDAO.updateEnrollment(enrollment);
    }
}