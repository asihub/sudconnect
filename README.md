# SUDConnect

> Privacy-preserving data integration pipeline for Substance Use Disorder (SUD) and Social Determinants of Health (SDOH) care coordination.

**License:** [Apache 2.0](LICENSE) — free to use, modify, and deploy  
**FHIR Standard:** [HL7 FHIR R4](https://hl7.org/fhir/R4/) — data exchange specification this project is built on  
**Key Regulation:** [42 CFR Part 2](https://www.ecfr.gov/current/title-42/chapter-I/subchapter-A/part-2) — federal confidentiality rules for SUD patient records

---

## The Problem

Patients with substance use disorders face a compounded crisis: their clinical treatment is often disconnected from the social factors — housing instability, food insecurity, lack of transportation — that directly determine whether treatment succeeds or fails.

The technical barrier is real and well-documented:

- **SUD data** is protected under 42 CFR Part 2, which imposes stricter confidentiality requirements than HIPAA
- **SDOH data** lives in separate systems: housing agencies, food banks, social services platforms
- No open-source, standards-based tool exists to bridge these two worlds in a compliant way

The result: care coordinators manually copy data between systems, consent is poorly tracked, and patients fall through the gaps.

---

## What SUDConnect Does

SUDConnect is an open-source pipeline that enables **privacy-preserving, consent-aware data exchange** between SUD clinical systems and SDOH service providers.

```
SUD EHR → Consent Engine (42 CFR Part 2) → FHIR Bundle Builder → SDOH Platform
                    ↑
            Minimum Necessary
              Data Filter
```

Key capabilities:

- **Consent Engine** — enforces 42 CFR Part 2 compliant consent before any data leaves the SUD system
- **FHIR R4 Normalization** — maps SUD clinical data (diagnosis, treatment history) and SDOH screening results (AHC-HRSN, PRAPARE) to standard FHIR resources
- **Minimum Necessary Filter** — ensures only the data required for SDOH coordination is shared, nothing more
- **Audit Log** — immutable, HIPAA-compliant log of every data access and consent decision
- **SDOH Routing** — routes patient needs to appropriate community resources based on screening results

---

## Why Now

The 2023 Consolidated Appropriations Act reformed 42 CFR Part 2 to better align with HIPAA and explicitly enable care coordination — but the technical infrastructure to implement this has not caught up. CMS Accountable Health Communities and SAMHSA's care integration initiatives are actively funding organizations to solve exactly this problem. SUDConnect provides the open-source foundation that these organizations need.

---

## Architecture

### Java / Spring Boot (Core Pipeline)

```
sudconnect-core/
├── consent/          # 42 CFR Part 2 consent validation engine
├── fhir/             # FHIR R4 resource builders and validators
├── filter/           # Minimum necessary data filtering
├── audit/            # Immutable audit logging
└── routing/          # SDOH service routing logic
```

### Python SDK

```
sudconnect-python/
├── client/           # REST client for SUDConnect API
├── analytics/        # SDOH screening analytics and reporting
└── cli/              # Command-line tools for integration testing
```

### Infrastructure (AWS)

- API Gateway + Lambda for serverless deployment option
- RDS (PostgreSQL) for consent registry
- CloudWatch for audit log persistence
- KMS for encryption at rest

---

## FHIR Resources

SUDConnect works with the following FHIR R4 resources:

| Resource | Usage |
|---|---|
| `Patient` | De-identified patient identity |
| `Consent` | 42 CFR Part 2 consent record |
| `Condition` | SUD diagnosis (ICD-10) |
| `Observation` | SDOH screening responses |
| `ServiceRequest` | Referral to SDOH services |
| `Bundle` | Complete care coordination package |

---

## Compliance

SUDConnect is designed with compliance as a core requirement, not an afterthought:

**42 CFR Part 2** — All data flows require explicit patient consent. The consent engine validates scope, expiration, and permitted disclosures before any data is shared.

**HIPAA** — Audit logs, encryption at rest and in transit, minimum necessary standard enforced at the data layer.

**HL7 FHIR R4** — All data structures follow HL7 FHIR R4 specification. Compatible with US Core and SDOH Clinical Care Implementation Guide.

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/your-org/sudconnect.git

# Run with Docker
docker-compose up

# Or build from source (requires Java 17+)
./mvnw spring-boot:run
```

Full documentation: [docs/getting-started.md](docs/getting-started.md)

---

## Roadmap

- [x] Core FHIR R4 data models
- [x] 42 CFR Part 2 consent engine (v1)
- [ ] HAPI FHIR Server integration
- [ ] Python SDK
- [ ] AHC-HRSN screening support
- [ ] PRAPARE screening support
- [ ] Reference integration with Epic Sandbox
- [ ] HL7 SDOH Clinical Care IG conformance testing

---

## Contributing

SUDConnect is built for the community. Contributions are welcome — especially from organizations working in SUD treatment, SDOH data collection, and healthcare interoperability.

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## Background

SUDConnect was created to address a gap observed directly in behavioral health systems: the technical and regulatory barriers that prevent SUD treatment providers from coordinating with social service organizations, even when the 2023 reforms to 42 CFR Part 2 explicitly enable such coordination.

The project aligns with federal initiatives including CMS Accountable Health Communities, SAMHSA's Behavioral Health Integration program, and the ONC interoperability roadmap.

---

## License

Apache 2.0 — free to use, modify, and deploy in your organization.

---

## Contact

For questions, pilot partnerships, or integration support: [open an issue](https://github.com/your-org/sudconnect/issues)
