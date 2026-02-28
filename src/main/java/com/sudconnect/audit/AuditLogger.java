package com.sudconnect.audit;

import com.sudconnect.model.AuditLog;
import com.sudconnect.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogger {

  private final AuditLogRepository auditLogRepository;

  public void logConsentValidated(String patientId, String recipientOrgId, String consentRecordId) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.CONSENT_VALIDATED)
        .recipientOrganizationId(recipientOrgId)
        .consentRecordId(consentRecordId)
        .performedAt(LocalDateTime.now())
        .build());
  }

  public void logConsentDenied(String patientId, String recipientOrgId, String reason) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.CONSENT_DENIED)
        .recipientOrganizationId(recipientOrgId)
        .performedAt(LocalDateTime.now())
        .details(reason)
        .build());
  }

  public void logConsentGranted(String patientId, String recipientOrgId, String consentRecordId) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.CONSENT_GRANTED)
        .recipientOrganizationId(recipientOrgId)
        .consentRecordId(consentRecordId)
        .performedAt(LocalDateTime.now())
        .build());
  }

  public void logConsentRevoked(String patientId, String consentRecordId) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.CONSENT_REVOKED)
        .consentRecordId(consentRecordId)
        .performedAt(LocalDateTime.now())
        .build());
  }

  public void logConsentExpired(String patientId, String consentRecordId) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.CONSENT_EXPIRED)
        .consentRecordId(consentRecordId)
        .performedAt(LocalDateTime.now())
        .build());
  }

  public void logDataDisclosed(String patientId, String recipientOrgId, String dataCategories) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.DATA_DISCLOSED)
        .recipientOrganizationId(recipientOrgId)
        .dataCategories(dataCategories)
        .performedAt(LocalDateTime.now())
        .build());
  }

  public void logDataFiltered(String patientId, String details) {
    save(AuditLog.builder()
        .patientIdHash(hash(patientId))
        .actionType(AuditLog.ActionType.DATA_FILTERED)
        .performedAt(LocalDateTime.now())
        .details(details)
        .build());
  }

  private void save(AuditLog auditLog) {
    try {
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      // Audit logging must never break the main flow
      log.error("Failed to write audit log: {}", e.getMessage(), e);
    }
  }

  private String hash(String patientId) {
    return DigestUtils.md5DigestAsHex(patientId.getBytes());
  }
}