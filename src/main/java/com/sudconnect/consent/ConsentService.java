package com.sudconnect.consent;

import com.sudconnect.audit.AuditLogger;
import com.sudconnect.model.ConsentRecord;
import com.sudconnect.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {

  private final ConsentRepository consentRepository;
  private final AuditLogger auditLogger;

  public ConsentValidationResult validate(
      String patientId,
      String recipientOrganizationId,
      ConsentRecord.DisclosureType disclosureType) {

    log.debug("Validating consent for patient={}, recipient={}, type={}",
        patientId, recipientOrganizationId, disclosureType);

    var consent = consentRepository
        .findByPatientIdAndRecipientOrganizationIdAndDisclosureTypeAndActiveTrue(
            patientId, recipientOrganizationId, disclosureType);

    if (consent.isEmpty()) {
      auditLogger.logConsentDenied(patientId, recipientOrganizationId,
          "No active consent found for this disclosure");
      return ConsentValidationResult.denied("No active consent found for this disclosure");
    }

    var record = consent.get();

    if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
      deactivateExpiredConsent(record);
      auditLogger.logConsentExpired(patientId, record.getId());
      auditLogger.logConsentDenied(patientId, recipientOrganizationId, "Consent has expired");
      return ConsentValidationResult.denied("Consent has expired");
    }

    auditLogger.logConsentValidated(patientId, recipientOrganizationId, record.getId());
    log.info("Consent validated successfully for patient={}", patientId);
    return ConsentValidationResult.approved(record);
  }

  @Transactional
  public ConsentRecord grant(ConsentRecord consent) {
    log.info("Granting consent for patient={}, recipient={}, type={}",
        consent.getPatientId(),
        consent.getRecipientOrganizationId(),
        consent.getDisclosureType());
    var saved = consentRepository.save(consent);
    auditLogger.logConsentGranted(consent.getPatientId(),
        consent.getRecipientOrganizationId(), saved.getId());
    return saved;
  }

  @Transactional
  public void revoke(String consentId) {
    consentRepository.findById(consentId).ifPresent(consent -> {
      consent.setActive(false);
      consentRepository.save(consent);
      auditLogger.logConsentRevoked(consent.getPatientId(), consentId);
      log.info("Consent revoked: id={}, patient={}", consentId, consent.getPatientId());
    });
  }

  public List<ConsentRecord> getActiveConsents(String patientId) {
    return consentRepository.findAllByPatientIdAndActiveTrue(patientId);
  }

  @Transactional
  public void expireStaleConsents() {
    var expired = consentRepository
        .findAllByExpiresAtBeforeAndActiveTrue(LocalDateTime.now());
    expired.forEach(this::deactivateExpiredConsent);
    log.info("Expired {} stale consent records", expired.size());
  }

  private void deactivateExpiredConsent(ConsentRecord record) {
    record.setActive(false);
    consentRepository.save(record);
    auditLogger.logConsentExpired(record.getPatientId(), record.getId());
    log.warn("Deactivated expired consent: id={}, patient={}",
        record.getId(), record.getPatientId());
  }
}