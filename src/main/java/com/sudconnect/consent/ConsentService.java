package com.sudconnect.consent;

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

  /**
   * Validates that a valid 42 CFR Part 2 compliant consent exists
   * before any SUD data is disclosed.
   */
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
      return ConsentValidationResult.denied("No active consent found for this disclosure");
    }

    var record = consent.get();

    if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
      deactivateExpiredConsent(record);
      return ConsentValidationResult.denied("Consent has expired");
    }

    log.info("Consent validated successfully for patient={}", patientId);
    return ConsentValidationResult.approved(record);
  }

  @Transactional
  public ConsentRecord grant(ConsentRecord consent) {
    log.info("Granting consent for patient={}, recipient={}, type={}",
        consent.getPatientId(),
        consent.getRecipientOrganizationId(),
        consent.getDisclosureType());
    return consentRepository.save(consent);
  }

  @Transactional
  public void revoke(String consentId) {
    consentRepository.findById(consentId).ifPresent(consent -> {
      consent.setActive(false);
      consentRepository.save(consent);
      log.info("Consent revoked: id={}, patient={}",
          consentId, consent.getPatientId());
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
    log.warn("Deactivated expired consent: id={}, patient={}",
        record.getId(), record.getPatientId());
  }
}