package com.sudconnect.fhir;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SdohScreeningData {

  private String patientId;
  private String screeningType;   // AHC-HRSN, PRAPARE
  private List<SdohNeed> needs;
}