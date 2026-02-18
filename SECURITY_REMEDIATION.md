# Security Remediation Spec — BlueSupply

## Background

GitHub Dependabot flagged **7 vulnerabilities** (1 critical, 2 high, 3 moderate, 1 low) across the `supply-svc` module. This spec defines the approach to assess and remediate each one systematically.

---

## Identified Vulnerable Dependencies

| Severity | Library | Current Version | Issue |
|---|---|---|---|
| **Critical** | `spring-security-oauth2-autoconfigure` | 2.6.8 | Deprecated Spring OAuth2 stack incompatible with Spring Boot 3.5; pulls in vulnerable transitive deps (CVE-2025-22235, CVE-2024-38820) |
| **High** | `io.jsonwebtoken:jjwt` | 0.9.1 | 6+ years outdated; flagged under CVE-2024-31033 (disputed but library is end-of-life) |
| **High** | `org.xhtmlrenderer:flying-saucer-pdf` | 9.1.22 | XXE injection vulnerability; depends on unmaintained iText 2.1.7 internally |
| **Moderate** | `com.itextpdf:itext7-core` | 7.2.3 | DoS CVEs in 7.2.x line (CVE-2022-24196, CVE-2022-24197); 7.2.4+ patched |
| **Moderate** | `com.itextpdf:html2pdf` | 4.0.3 | Aligns with vulnerable iText7 core |
| **Moderate** | `com.mashape.unirest:unirest-java` | 1.4.9 | Abandoned library; no security patches since ~2016 |
| **Low** | `org.testcontainers:postgresql` | 1.17.3 | Minor transitive vulnerability; test scope only |

---

## Remediation Plan

### Phase 1 — Critical: Replace Deprecated OAuth2 Stack `[ TODO ]`

**Problem:** `spring-security-oauth2-autoconfigure:2.6.8` is a Spring Boot 2.x era library used inside a Spring Boot 3.5 project. It is end-of-life, pulls in vulnerable transitive dependencies, and conflicts with the native OAuth2 support already included in Spring Boot 3.x.

**Approach:**
1. Remove `spring-security-oauth2-autoconfigure` from `supply-svc/pom.xml`
2. Migrate to Spring Boot 3.x native OAuth2 — `spring-boot-starter-oauth2-resource-server` (already partially in the dependency tree via `spring-boot-starter-oauth2-client`)
3. Refactor `security/config/` and `auth/` packages to use the new `SecurityFilterChain` bean-based configuration (replacing deprecated `WebSecurityConfigurerAdapter`)
4. Validate that all role/privilege-gated endpoints behave identically after migration

**Risk:** High — touches authentication infrastructure. Requires thorough testing of all 22 controllers' access control.

---

### Phase 2 — High: Replace `jjwt 0.9.1` with Modern JWT Library `[ TODO ]`

**Problem:** `jjwt:0.9.1` is from 2016. The API changed significantly in `0.11.x` and again in `0.12.x`. While CVE-2024-31033 was disputed/withdrawn, the library is unmaintained at this version and Dependabot still flags it.

**Approach:**
1. Upgrade to `io.jsonwebtoken:jjwt-api:0.12.6` + `jjwt-impl:0.12.6` + `jjwt-jackson:0.12.6` (the new split-artifact model)
2. Refactor JWT generation and parsing in `auth/` and `security/` packages — the `0.12.x` API uses a builder/parser pattern that differs from `0.9.1`
3. Key changes: `Jwts.parser().setSigningKey()` → `Jwts.parser().verifyWith()`, `Jwts.builder().signWith(key, algorithm)` explicit algorithm required

**Risk:** Medium — isolated to `auth/` and `security/` packages. JWT token format remains compatible.

---

### Phase 3 — High: Resolve PDF Library XXE Vulnerability `[ TODO ]`

**Problem:** `flying-saucer-pdf:9.1.22` is vulnerable to XXE (XML External Entity) injection when parsing untrusted HTML/XML input. It internally depends on an unmaintained iText 2.1.7.

**Approach:**
1. Evaluate whether Flying Saucer is actually processing untrusted user input or only internal Thymeleaf-rendered templates
   - If **only internal templates**: document the constraint and disable external entity resolution as a defence-in-depth measure
   - If **any user-supplied HTML reaches Flying Saucer**: treat as critical and replace
2. Upgrade `flying-saucer-pdf` to `9.3.x` which uses OpenPDF (safe, maintained fork) instead of iText 2.1.7
3. Ensure the 8 Thymeleaf templates render correctly after the upgrade

**Risk:** Low-Medium — PDF rendering is self-contained in utility/service classes. Regression-test all document generation endpoints (LPO, GRN, Quotation).

---

### Phase 4 — Moderate: Upgrade iText7 and html2pdf `[ TODO ]`

**Problem:** `itext7-core:7.2.3` has DoS vulnerabilities (CVE-2022-24196, CVE-2022-24197) fixed in `7.2.4`. `html2pdf:4.0.3` tracks the same iText7 version.

**Approach:**
1. Upgrade `com.itextpdf:itext7-core` to `7.2.6` (latest stable 7.2.x)
2. Upgrade `com.itextpdf:html2pdf` to `4.0.6` (compatible with iText7 7.2.x)
3. Verify BouncyCastle exclusions remain intact (currently explicitly excluded in pom.xml)
4. Run regression tests on PDF output for LPO and GRN templates

**Risk:** Low — minor version upgrades within a stable line.

---

### Phase 5 — Moderate: Replace Abandoned `unirest-java` `[ DONE ]`

**Problem:** `com.mashape.unirest:unirest-java:1.4.9` has not received security patches since ~2016. The project was forked to `kong.unirest:unirest-java`.

**Approach:**
1. Locate all usages of Unirest in the codebase (likely in email or supplier notification utilities)
2. Option A: Migrate to `kong.unirest:unirest-java:4.x` (maintained fork, similar API)
3. Option B: Replace with Spring's built-in `RestClient` (Spring Boot 3.2+) — preferred since it removes an external dependency entirely

**Risk:** Low — scoped to HTTP client call sites.

**Resolution:** Dependency was not imported anywhere in the source code — removed entirely from `supply-svc/pom.xml`. Commit: `c8c952d`.

---

### Phase 6 — Low: Bump Test Dependency `[ DONE ]`

**Problem:** `org.testcontainers:postgresql:1.17.3` has a minor vulnerability. Scope is `test` only — no production impact.

**Approach:**
1. Upgrade to `org.testcontainers:postgresql:1.20.x` in `supply-svc/pom.xml`

**Risk:** Minimal — test scope only.

**Resolution:** Upgraded to `1.20.4` in `supply-svc/pom.xml`. Commit: `ad671bd`.

---

## Execution Order

```
Phase 6 → Phase 5 → Phase 4 → Phase 3 → Phase 2 → Phase 1
```

Start with low-risk, isolated changes to build confidence, and tackle the critical OAuth2 migration last with the most thorough testing.

---

## Testing Requirements Per Phase

| Phase | Testing Focus |
|---|---|
| Phase 1 | Full authentication and authorization regression across all 22 controllers |
| Phase 2 | JWT issuance, validation, expiry, and refresh flows |
| Phase 3 & 4 | All PDF export endpoints — LPO, GRN, Quotation, Reports |
| Phase 5 | Any endpoint triggering outbound HTTP calls (email, supplier RFQ) |
| Phase 6 | Full test suite to confirm no test infrastructure regressions |

---

## Progress

| Phase | Status | Commit |
|---|---|---|
| Phase 6 — Bump testcontainers | ✅ Done | `ad671bd` |
| Phase 5 — Remove unirest-java | ✅ Done | `c8c952d` |
| Phase 4 — Upgrade iText7 + html2pdf | ⬜ TODO | — |
| Phase 3 — Fix Flying Saucer XXE | ⬜ TODO | — |
| Phase 2 — Upgrade jjwt | ⬜ TODO | — |
| Phase 1 — Replace OAuth2 stack | ⬜ TODO | — |

---

## Resume Instructions for Claude

All work is on branch `add-claude-md`. To continue, pick up from **Phase 4** and work upward to Phase 1.

**Phase 4 — next immediate step:**
- In `supply-svc/pom.xml`, upgrade `com.itextpdf:itext7-core` from `7.2.3` → `7.2.6` and `com.itextpdf:html2pdf` from `4.0.3` → `4.0.6`
- Preserve the existing BouncyCastle exclusions on `itext7-core`
- Commit, then move to Phase 3
