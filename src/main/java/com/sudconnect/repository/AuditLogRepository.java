package com.sudconnect.repository;

import com.sudconnect.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

  List<AuditLog> findAllByPatientIdHashOrderByPerformedAtDesc(String patientIdHash);

  List<AuditLog> findAllByPerformedAtBetweenOrderByPerformedAtDesc(
      LocalDateTime from,
      LocalDateTime to
  );

  List<AuditLog> findAllByActionTypeOrderByPerformedAtDesc(AuditLog.ActionType actionType);
}