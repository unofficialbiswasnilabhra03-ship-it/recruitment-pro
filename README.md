# RecruitPro — Recruitment Management System

A **production-ready** backend built with **Java 21** and **Spring Boot 3.x** that
streamlines end-to-end hiring: job posting → candidate application → interview
scheduling → feedback → offer/reject.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.x, Spring MVC, Spring Security |
| Auth | JWT (jjwt 0.12.x) + DB-backed Refresh Tokens |
| ORM | Spring Data JPA / Hibernate with `@SQLDelete` soft delete |
| Database | MySQL 8 |
| Mapping | MapStruct |
| Validation | Jakarta Validation |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Email | Spring Mail (Gmail SMTP) |
| File Storage | Local FS (dev) / AWS S3 (prod) |
| Scheduler | Spring `@Scheduled` |
| Boilerplate | Lombok |
| Testing | JUnit 5, Mockito, Spring Security Test |
| Containerisation | Docker, Docker Compose |

---

## Project Structure

```
com.recruitpro
├── config          # SecurityConfig, SwaggerConfig, JpaAuditingConfig, DataInitializer
├── security        # JwtUtil, JwtFilter, UserPrincipal, CustomUserDetailsService
├── audit           # Auditable base class, AuditorAwareImpl
├── entity          # 16 JPA entities
├── enums           # 10 enums (RoleName, JobStatus, ApplicationStatus …)
├── repository      # 16 Spring Data JPA repositories with custom JPQL
├── dto
│   ├── request     # 18 validated request bodies
│   └── response    # 12 response DTOs + ApiResponse<T> envelope
├── service
│   ├── interfaces  # 10 service contracts
│   └── impl        # 10 implementations
├── controller      # 10 REST controllers (~70 endpoints)
├── exception       # GlobalExceptionHandler + 4 domain exceptions
├── constants       # AppConstants
└── scheduler       # InterviewReminderScheduler
```

---

## Roles

| Role | Capabilities |
|---|---|
| `ROLE_ADMIN` | Full access; user management; global dashboard; email logs |
| `ROLE_HR` | Manage companies, jobs, applications, schedule interviews, company dashboard |
| `ROLE_INTERVIEWER` | View assigned interviews, submit feedback |
| `ROLE_CANDIDATE` | Browse jobs, apply, manage profile / resume / sub-profile sections |

Admin is seeded via `DataInitializer` on first startup. All other roles self-register.

---


## Quick Start (Local)

**Prerequisites:** Java 21, Maven 3.9+, MySQL 8

```bash
# 1. Create database
mysql -u root -p -e "CREATE DATABASE recruitpro_dev;"

# 2. Set credentials in src/main/resources/application-dev.properties

# 3. Run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## API Overview (~70 Endpoints)

### Auth  `/api/v1/auth`
| Method | Path | Description |
|---|---|---|
| POST | `/register` | Register (HR / Interviewer / Candidate) |
| POST | `/login` | Login → access + refresh token |
| POST | `/refresh-token` | Rotate refresh token |
| POST | `/logout` | Revoke refresh token |
| POST | `/forgot-password` | Send reset email |
| POST | `/reset-password` | Reset with token |

### Users  `/api/v1/users`
| Method | Path | Auth |
|---|---|---|
| GET | `/me` | Any |
| PUT | `/me` | Any |
| GET | `/` | Admin |
| GET `/ {id}` | Admin |
| PATCH | `/{id}/enable` | Admin |
| PATCH | `/{id}/disable` | Admin |
| DELETE | `/{id}` | Admin |

### Companies  `/api/v1/companies`
POST, PUT, DELETE → HR/Admin · GET → Public

### Jobs  `/api/v1/jobs`
POST, PUT, PATCH `/close`, DELETE → HR/Admin · GET → Public  
Supports: `?search=`, `?jobType=`, `?experienceLevel=`, `?location=`, `?salaryMin=`, `?salaryMax=`

### Candidates  `/api/v1/candidates`
`/me/**` → Candidate · GET list/search/filter → HR/Admin/Interviewer  
Sub-resources: `/me/education`, `/me/experience`, `/me/skills`, `/me/portfolio`

### Resumes  `/api/v1/resumes`
POST/GET/DELETE `/me` → Candidate · GET `/candidate/{id}` + `/download` → HR/Admin/Interviewer

### Applications  `/api/v1/applications`
POST, DELETE `/withdraw`, GET `/me` → Candidate  
GET `/job/{id}`, `/company/{id}`, PATCH `/status`, PATCH `/shortlist` → HR/Admin

### Interviews  `/api/v1/interviews`
POST, PUT `/reschedule`, PATCH `/cancel` → HR/Admin  
GET/POST `/me`, GET `/me/upcoming`, POST `/{id}/feedback` → Interviewer

### Dashboard  `/api/v1/dashboard`
GET `/` → Admin · GET `/company/{id}` → HR/Admin

### Notifications  `/api/v1/notifications`
GET `/`, `/unread`, `/unread/count` · PATCH `/{id}/read`, `/read-all` → Any authenticated

### Email Logs  `/api/v1/emails`
GET `/`, `/{id}`, `/recipient/{email}` → Admin

---

## Key Architecture Decisions

**Role-only authorization** — no permissions table; four roles cover all access patterns cleanly.

**Soft delete on Company, Candidate, Job** — `@SQLDelete` sets `deleted_at`; `@Filter` excludes
soft-deleted rows from queries transparently. Unique constraints work correctly because
soft-deleted rows are excluded at query level.

**DB-backed refresh tokens** — tokens stored in `refresh_tokens` table so logout actually
invalidates sessions server-side. Rotation on every refresh.

**One resume per candidate** — simpler model; HR always sees the candidate's latest resume
regardless of which job they applied to.

**Hire recommendation on InterviewFeedback** — each round has a `HireRecommendation` enum
(STRONG_HIRE → STRONG_NO_HIRE). HR rolls up all rounds when making the final call and
updates `JobApplication.status` to OFFER_EXTENDED / HIRED.

**Normalized candidate sub-profile tables** — `candidate_education`, `candidate_experience`,
`candidate_skills`, `candidate_portfolio` are separate queryable tables (not JSON blobs),
enabling `?skill=Java` filtering at the DB level.

**Email is async** — `@Async` on every email method; failures are logged to `email_logs`
without breaking the request flow.

**AuditorAware** — `createdBy`/`updatedBy` auto-populated from `SecurityContextHolder`;
falls back to `"SYSTEM"` for scheduler / seed operations.

---

## Interview Talking Points

- How JWT stateless auth works with Spring Security filter chain
- Why refresh tokens are stored in the DB (revocability) vs. staying in memory
- `@SQLDelete` + `@Filter` for soft delete without polluting all queries
- `@EnableMethodSecurity` + `@PreAuthorize` vs. URL-level rules — when to use which
- `AuditorAware` and `@EntityListeners` for audit trail without manual setters
- MapStruct vs. ModelMapper trade-offs (compile-time vs. runtime)
- Why `@Async` email sending + `EmailLog` beats synchronous inline sends
- Pagination/sorting strategy: `Pageable` + `Page<T>` throughout
- JPQL vs. native query decisions in repositories
- `ApiResponse<T>` generic envelope for consistent client parsing
