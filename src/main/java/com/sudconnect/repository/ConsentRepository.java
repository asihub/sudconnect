package com.sudconnect.repository;

import com.sudconnect.model.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<ConsentRecord, String> {

  Optional<ConsentRecord> findByPatientIdAndRecipientOrganizationIdAndDisclosureTypeAndActiveTrue(
      String patientId,
      String recipientOrganizationId,
      ConsentRecord.DisclosureType disclosureType
  );

  List<ConsentRecord> findAllByPatientIdAndActiveTrue(String patientId);

  List<ConsentRecord> findAllByExpiresAtBeforeAndActiveTrue(LocalDateTime now);
}