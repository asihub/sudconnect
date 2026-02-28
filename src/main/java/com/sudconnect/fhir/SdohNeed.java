package com.sudconnect.fhir;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SdohNeed {

  private String category;      // HOUSING, FOOD, TRANSPORTATION
  private String loincCode;
  private String snomedCode;
  private String display;

  // Common SDOH needs with standard codes
  public static SdohNeed housing() {
    return SdohNeed.builder()
        .category("HOUSING")
        .loincCode("71802-3")
        .snomedCode("160695008")
        .display("Housing instability")
        .build();
  }

  public static SdohNeed food() {
    return SdohNeed.builder()
        .category("FOOD")
        .loincCode("88122-7")
        .snomedCode("445281000124101")
        .display("Food insecurity")
        .build();
  }

  public static SdohNeed transportation() {
    return SdohNeed.builder()
        .category("TRANSPORTATION")
        .loincCode("93030-5")
        .snomedCode("160695008")
        .display("Transportation insecurity")
        .build();
  }
}