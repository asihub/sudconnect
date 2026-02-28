package com.sudconnect.controller;

import com.sudconnect.audit.AuditLogger;
import com.sudconnect.consent.ConsentService;
import com.sudconnect.fhir.FhirBundleBuilder;
import com.sudconnect.fhir.SdohNeed;
import com.sudconnect.fhir.SdohScreeningData;
import com.sudconnect.filter.MinimumNecessaryFilter;
import com.sudconnect.model.ConsentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fhir")
@RequiredArgsConstructor
public class FhirController {

  private final FhirBundleBuilder fhirBundleBuilder;
  private final ConsentService consentService;
  private final AuditLogger auditLogger;
  private final MinimumNecessaryFilter minimumNecessaryFilter;

  @PostMapping("/bundle/{patientId}")
  public ResponseEntity<String> buildBundle(
      @PathVariable String patientId,
      @RequestParam String recipientOrganizationId,
      @RequestParam ConsentRecord.DisclosureType disclosureType,
      @RequestBody List<String> needs) {

    var validation = consentService.validate(patientId, recipientOrganizationId, disclosureType);

    if (!validation.isValid()) {
      auditLogger.logConsentDenied(patientId, recipientOrganizationId, validation.getReason());
      return ResponseEntity.status(403).body(validation.getReason());
    }

    var sdohNeeds = needs.stream()
        .map(this::mapNeed)
        .filter(need -> need != null)
        .toList();

    var screeningData = SdohScreeningData.builder()
        .patientId(patientId)
        .screeningType("AHC-HRSN")
        .needs(sdohNeeds)
        .build();

    var bundle = fhirBundleBuilder.buildSdohCoordinationBundle(
        patientId,
        validation.getConsentRecord(),
        screeningData
    );

    // Apply minimum necessary filter
    var filteredBundle = minimumNecessaryFilter.filter(bundle, validation.getConsentRecord());

    auditLogger.logDataDisclosed(patientId, recipientOrganizationId, String.join(",", needs));
    auditLogger.logDataFiltered(patientId,
        "Applied minimum necessary filter, " +
            (bundle.getEntry().size() - filteredBundle.getEntry().size()) +
            " entries removed");

    return ResponseEntity.ok(fhirBundleBuilder.bundleToJson(filteredBundle));
  }

  private SdohNeed mapNeed(String need) {
    return switch (need.toUpperCase()) {
      case "HOUSING" -> SdohNeed.housing();
      case "FOOD" -> SdohNeed.food();
      case "TRANSPORTATION" -> SdohNeed.transportation();
      default -> {
        yield null;
      }
    };
  }
}
