package com.metabirth.dao;

import com.metabirth.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {
    private static final Logger log = LoggerFactory.getLogger(EnrollmentDAO.class);
    private final Connection connection;

    // 생성자를 통한 Connection 주입
    public EnrollmentDAO(Connection connection) {
        this.connection = connection;
    }

    // 모든 수강신청 조회
    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE status = 0";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp updatedTs = rs.getTimestamp("updated_at");
                LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                Timestamp deletedTs = rs.getTimestamp("deleted_at");
                LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                enrollments.add(new Enrollment(
                        rs.getInt("enrollment_id"),
                        rs.getInt("student_id"),
                        rs.getInt("class_id"),
                        rs.getByte("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        updatedAt,
                        deletedAt
                ));
            }
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 전체 조회 실패 - 사유: {}", e.getMessage());
            return null;
        }
        return enrollments;
    }

    // 특정 수강신청 조회
    public Enrollment getEnrollmentById(int enrollmentId) {
        Enrollment enrollment = null;
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ? AND status = 0";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
                    Timestamp deletedTs = rs.getTimestamp("deleted_at");
                    LocalDateTime deletedAt = deletedTs != null ? deletedTs.toLocalDateTime() : null;
                    enrollment = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getInt("class_id"),
                            rs.getByte("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            updatedAt,
                            deletedAt
                    );
                }
            }
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 단건 조회 실패 - ID: {}, 사유: {}", enrollmentId, e.getMessage());
            return null;
        }
        return enrollment;
    }

    // 수강신청 삽입
    /* enrollment_id : 고유키, AUTO_INCREMENT
     * status : 0-활성 상태(디폴트값), 1-삭제 상태
     * create_at : 디폴트값 now()
     * */
    public boolean addEnrollment(Enrollment enrollment) {
        String sql = """
                INSERT INTO enrollments (student_id, class_id)
                VALUES (?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getClassId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        enrollment.setEnrollmentId(generatedKeys.getInt(1));
                    }
                }
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 등록 실패 - studentId: {}, classId: {}, 사유: {}",
                    enrollment.getStudentId(), enrollment.getClassId(), e.getMessage());
            return false;
        }
    }

    // 수강신청 업데이트
    public boolean updateEnrollment(Enrollment enrollment) {
        String sql = """
                UPDATE enrollments SET student_id = ?, class_id = ?, updated_at = now() 
                WHERE enrollment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getClassId());
            ps.setInt(3, enrollment.getEnrollmentId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 수정 실패 - ID: {}, 사유: {}", enrollment.getEnrollmentId(), e.getMessage());
            return false;
        }
    }

    // 수강신청 삭제 (논리적 삭제)
    public boolean deleteEnrollment(int enrollmentId) {
        String sql = """
                UPDATE enrollments SET status = 1, updated_at = now(), deleted_at = now()
                WHERE enrollment_id = ? AND status = 0
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            int affectedRows = ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 삭제 실패 - ID: {}, 사유: {}", enrollmentId, e.getMessage());
            return false;
        }
    }

    // 수강신청이 활성 상태/삭제 상태인지 확인하는 메소드
    public boolean isActiveEnrollment(int enrollmentId) {
        String sql = """
                SELECT COUNT(*) FROM enrollments WHERE enrollment_id = ? AND status = 0
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.warn("[DAO] 수강신청 활성 여부 확인 실패 - ID: {}, 사유: {}", enrollmentId, e.getMessage());
        }

        return false;
    }
}