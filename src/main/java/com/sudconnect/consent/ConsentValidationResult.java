package com.sudconnect.consent;

import com.sudconnect.model.ConsentRecord;
import lombok.Getter;

@Getter
public class ConsentValidationResult {

  private final boolean valid;
  private final String reason;
  private final ConsentRecord consentRecord;

  private ConsentValidationResult(boolean valid, String reason, ConsentRecord consentRecord) {
    this.valid = valid;
    this.reason = reason;
    this.consentRecord = consentRecord;
  }

  public static ConsentValidationResult approved(ConsentRecord record) {
    return new ConsentValidationResult(true, null, record);
  }

  public static ConsentValidationResult denied(String reason) {
    return new ConsentValidationResult(false, reason, null);
  }
}