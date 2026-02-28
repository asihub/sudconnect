package com.sudconnect.filter;

import com.sudconnect.model.ConsentRecord;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MinimumNecessaryFilter {

  /**
   * Filters FHIR Bundle to contain only minimum necessary data
   * as required by 42 CFR Part 2 and HIPAA Minimum Necessary Standard.
   */
  public Bundle filter(Bundle bundle, ConsentRecord consentRecord) {
    var permittedCategories = parsePermittedCategories(
        consentRecord.getPermittedDataCategories()
    );

    log.debug("Filtering bundle id={} for permitted categories={}",
        bundle.getId(), permittedCategories);

    var filteredBundle = new Bundle();
    filteredBundle.setId(bundle.getId());
    filteredBundle.setType(bundle.getType());
    filteredBundle.setTimestamp(bundle.getTimestamp());

    int removedCount = 0;

    for (var entry : bundle.getEntry()) {
      var resource = entry.getResource();

      // Always include Patient and Consent — required for coordination
      if (resource instanceof Patient || resource instanceof Consent) {
        filteredBundle.addEntry().setResource(resource);
        continue;
      }

      // Filter Observations by permitted categories
      if (resource instanceof Observation observation) {
        if (isObservationPermitted(observation, permittedCategories)) {
          filteredBundle.addEntry().setResource(resource);
        } else {
          removedCount++;
          log.debug("Filtered out Observation id={}", observation.getId());
        }
        continue;
      }

      // Filter ServiceRequests by permitted categories
      if (resource instanceof ServiceRequest serviceRequest) {
        if (isServiceRequestPermitted(serviceRequest, permittedCategories)) {
          filteredBundle.addEntry().setResource(resource);
        } else {
          removedCount++;
          log.debug("Filtered out ServiceRequest id={}", serviceRequest.getId());
        }
      }
    }

    log.info("Bundle filtered: removed {} entries, {} remaining",
        removedCount, filteredBundle.getEntry().size());

    return filteredBundle;
  }

  private boolean isObservationPermitted(
      Observation observation,
      List<String> permittedCategories) {

    return observation.getCode().getCoding().stream()
        .anyMatch(coding -> permittedCategories.stream()
            .anyMatch(permitted ->
                coding.getDisplay() != null &&
                    coding.getDisplay().toUpperCase()
                        .contains(permitted.toUpperCase())
            )
        );
  }

  private boolean isServiceRequestPermitted(
      ServiceRequest serviceRequest,
      List<String> permittedCategories) {

    return serviceRequest.getCode().getCoding().stream()
        .anyMatch(coding -> permittedCategories.stream()
            .anyMatch(permitted ->
                coding.getDisplay() != null &&
                    coding.getDisplay().toUpperCase()
                        .contains(permitted.toUpperCase())
            )
        );
  }

  private List<String> parsePermittedCategories(String permittedDataCategories) {
    if (permittedDataCategories == null || permittedDataCategories.isBlank()) {
      return List.of();
    }
    return Arrays.stream(permittedDataCategories.split(","))
        .map(String::trim)
        .collect(Collectors.toList());
  }
}