# SolveSync

알고리즘 문제풀이 스터디 운영을 자동화하는 서비스입니다.  
구글 소셜 로그인(OAuth2)으로 인증하고, 서버가 발급한 JWT(Access Token)로 모든 보호 API를 호출합니다.

- 스터디 룸 생성(DRAFT → ACTIVE)
- 플랫폼/핸들(handle) 등록 (BOJ, Codeforces, Programmers 등)
- 초대(Invitation) / 가입신청(Join Request)
- 외부 수집 서버(FastAPI)가 전달한 “풀이 이벤트(팩트)”를 저장/집계
- 룸 규칙(기간/요구 문제 수/난이도 범위/공휴일 포함 등)을 만족했는지 평가

---

## 1. 핵심 개념

### 1) User
- 구글 로그인 성공 시 내부 사용자(User)가 생성됩니다.
- 내부 식별자는 `User.id`이며 **서비스의 모든 권한/연산의 기준**이 됩니다.
- 사용자는 공개 식별자 `username`을 설정할 수 있습니다(초대/검색에 사용).

### 2) Platform Account (유저의 플랫폼 핸들)
- 사용자는 플랫폼별(handle)을 등록합니다.  
  예) BOJ: `wlals123`, CODEFORCES: `tourist`
- 수집 서버는 이 정보를 기반으로 풀이 이벤트를 수집/전달합니다.

### 3) Study Room
- 방 생성 시 기본 상태는 `DRAFT` 입니다.
- `ACTIVE`로 전환된 이후부터 규칙 판정이 시작됩니다.
- 방은 `inviteCode`를 가지며, 공개방(listed=true)이면 목록에서 노출됩니다.

### 4) Rule Evaluation
- 외부 수집 서버가 “풀이 이벤트(팩트)”를 제공하면, SolveSync는 이를 저장/집계합니다.
- 방의 규칙(기간 단위/요구 개수/공휴일 포함 여부/플랫폼별 난이도 범위 등)에 따라 달성 여부를 평가합니다.
- 운영 스펙: 방이 ACTIVE로 전환되면 **“익일 0시(룸 타임존 기준)”부터 평가를 시작**합니다.
    - ACTIVE 전환 당일의 부분 시간은 평가에 포함하지 않습니다.

---

## 2. 기술 스택

- **Java 17**
- **Spring Boot 4.x**
- Spring Security + **OAuth2 Client (Google Login)**
- **JWT (JJWT)**
- Spring Web (REST API)
- Spring Data JPA / Hibernate
- MySQL 8
- RabbitMQ (수집 이벤트 비동기 적재/처리)
- Springdoc OpenAPI (Swagger UI)

---

## 3. 아키텍처 개요

- SolveSync (Spring Boot)
    - 사용자/방/멤버십/초대/신청 API 제공
    - JWT 인증 기반 보호 API 제공
    - 평가 스케줄러(또는 수동 run-now)로 규칙 판정 수행
    - 외부 수집 서버로부터 이벤트를 내부 API로 수신 후 MQ에 적재

- Collector (FastAPI, 외부 서버)
    - SolveSync의 플랫폼 계정(handle) 목록을 기반으로 풀이 이벤트 수집
    - 수집 결과를 SolveSync 내부 API로 전달(push)
    - 필요 시 SolveSync 내부 레지스트리를 pull하여 복구 가능


### JWT Secret 생성 예시 (macOS/Linux)
```bash
openssl rand -base64 48
