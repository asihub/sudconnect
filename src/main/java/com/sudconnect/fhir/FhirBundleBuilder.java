package com.sudconnect.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.sudconnect.model.ConsentRecord;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class FhirBundleBuilder {

  private final FhirContext fhirContext;
  private final IParser jsonParser;

  public FhirBundleBuilder() {
    this.fhirContext = FhirContext.forR4();
    this.jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
  }

  /**
   * Builds a FHIR R4 Bundle for SDOH care coordination
   * containing only minimum necessary data as required by 42 CFR Part 2
   */
  public Bundle buildSdohCoordinationBundle(
      String patientId,
      ConsentRecord consentRecord,
      SdohScreeningData screeningData) {

    var bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.COLLECTION);
    bundle.setTimestamp(new Date());

    // Patient resource - de-identified
    bundle.addEntry().setResource(buildPatient(patientId));

    // Consent resource - 42 CFR Part 2 reference
    bundle.addEntry().setResource(buildConsent(consentRecord));

    // SDOH Observations from screening
    if (screeningData != null) {
      screeningData.getNeeds().forEach(need ->
          bundle.addEntry().setResource(buildSdohObservation(patientId, need))
      );

      // ServiceRequest for each identified need
      screeningData.getNeeds().forEach(need ->
          bundle.addEntry().setResource(buildServiceRequest(patientId, need))
      );
    }

    log.info("Built FHIR Bundle id={} with {} entries for patient={}",
        bundle.getId(), bundle.getEntry().size(), patientId);

    return bundle;
  }

  public String bundleToJson(Bundle bundle) {
    return jsonParser.encodeResourceToString(bundle);
  }

  private Patient buildPatient(String patientId) {
    var patient = new Patient();
    patient.setId(UUID.randomUUID().toString());

    // Store original ID as identifier — not as name or DOB
    // Minimum necessary: only what SDOH coordination requires
    patient.addIdentifier()
        .setSystem("urn:sudconnect:patient")
        .setValue(patientId);

    return patient;
  }

  private org.hl7.fhir.r4.model.Consent buildConsent(ConsentRecord consentRecord) {
    var consent = new org.hl7.fhir.r4.model.Consent();
    consent.setId(consentRecord.getId());
    consent.setStatus(org.hl7.fhir.r4.model.Consent.ConsentState.ACTIVE);

    // 42 CFR Part 2 policy reference
    consent.addPolicy()
        .setUri("https://www.ecfr.gov/current/title-42/chapter-I/subchapter-A/part-2");

    // Validity period via provision
    var provision = new org.hl7.fhir.r4.model.Consent.provisionComponent();
    provision.getPeriod()
        .setStart(Date.from(consentRecord.getConsentGrantedAt()
            .atZone(ZoneId.systemDefault()).toInstant()))
        .setEnd(Date.from(consentRecord.getExpiresAt()
            .atZone(ZoneId.systemDefault()).toInstant()));
    consent.setProvision(provision);

    return consent;
  }

  private Observation buildSdohObservation(String patientId, SdohNeed need) {
    var observation = new Observation();
    observation.setId(UUID.randomUUID().toString());
    observation.setStatus(Observation.ObservationStatus.FINAL);

    observation.addCategory()
        .addCoding()
        .setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes")
        .setCode("sdoh-category-unspecified")
        .setDisplay("SDOH Category");

    observation.getCode()
        .addCoding()
        .setSystem("http://loinc.org")
        .setCode(need.getLoincCode())
        .setDisplay(need.getDisplay());

    observation.getSubject().setReference("Patient/" + patientId);
    observation.setEffective(new DateTimeType(new Date()));

    // Fix: wrap Coding inside CodeableConcept
    var valueCodeableConcept = new CodeableConcept();
    valueCodeableConcept.addCoding()
        .setSystem("http://loinc.org")
        .setCode("LA33-6")
        .setDisplay("Yes");
    observation.setValue(valueCodeableConcept);

    return observation;
  }

  private ServiceRequest buildServiceRequest(String patientId, SdohNeed need) {
    var serviceRequest = new ServiceRequest();
    serviceRequest.setId(UUID.randomUUID().toString());
    serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

    serviceRequest.getCode()
        .addCoding()
        .setSystem("http://snomed.info/sct")
        .setCode(need.getSnomedCode())
        .setDisplay(need.getDisplay());

    serviceRequest.getSubject().setReference("Patient/" + patientId);

    return serviceRequest;
  }
}