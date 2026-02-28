package com.sudconnect.controller;

import com.sudconnect.consent.ConsentService;
import com.sudconnect.consent.ConsentValidationResult;
import com.sudconnect.model.ConsentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
public class ConsentController {

  private final ConsentService consentService;

  @PostMapping
  public ResponseEntity<ConsentRecord> grant(@RequestBody ConsentRecord consent) {
    return ResponseEntity.ok(consentService.grant(consent));
  }

  @GetMapping("/validate")
  public ResponseEntity<ConsentValidationResult> validate(
      @RequestParam String patientId,
      @RequestParam String recipientOrganizationId,
      @RequestParam ConsentRecord.DisclosureType disclosureType) {

    return ResponseEntity.ok(
        consentService.validate(patientId, recipientOrganizationId, disclosureType)
    );
  }

  @GetMapping("/{patientId}")
  public ResponseEntity<List<ConsentRecord>> getActiveConsents(@PathVariable String patientId) {
    return ResponseEntity.ok(consentService.getActiveConsents(patientId));
  }

  @DeleteMapping("/{consentId}")
  public ResponseEntity<Void> revoke(@PathVariable String consentId) {
    consentService.revoke(consentId);
    return ResponseEntity.noContent().build();
  }
}