package com.sudconnect.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String patientIdHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActionType actionType;

  private String recipientOrganizationId;

  private String dataCategories;

  private String consentRecordId;

  @Column(nullable = false)
  private LocalDateTime performedAt;

  @Column(length = 2000)
  private String details;

  public enum ActionType {
    CONSENT_VALIDATED,
    CONSENT_DENIED,
    CONSENT_GRANTED,
    CONSENT_REVOKED,
    CONSENT_EXPIRED,
    DATA_DISCLOSED,
    DATA_FILTERED
  }
}