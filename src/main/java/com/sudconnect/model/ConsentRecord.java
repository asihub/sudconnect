package com.sudconnect.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consent_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String patientId;

  @Column(nullable = false)
  private String recipientOrganizationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DisclosureType disclosureType;

  @Column(nullable = false)
  private LocalDateTime consentGrantedAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private boolean active;

  // 42 CFR Part 2 specific fields
  @Column(nullable = false)
  private String authorizedBy;

  private String purposeOfDisclosure;

  @Column(length = 1000)
  private String permittedDataCategories;

  public enum DisclosureType {
    TREATMENT,
    PAYMENT,
    OPERATIONS,
    RESEARCH,
    EMERGENCY
  }
}