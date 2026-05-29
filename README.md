# course-lock-lab

Spring Boot와 MySQL을 기반으로 수강신청 피크 트래픽에서 발생하는 동시성 문제를 재현하고, 여러 동시성 제어 전략을 비교하기 위한 실험 프로젝트입니다.

핵심 시나리오는 정원 30명인 인기 강의에 1,000명의 학생이 동시에 수강신청을 요청하는 상황입니다. 단순 CRUD 구현보다 정원 초과, 중복 신청, 카운트 불일치 같은 일관성 실패를 관찰하고 개선 전략별 결과를 비교하는 데 목적이 있습니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- MySQL
- Redis
- Flyway
- Spring Boot Actuator
- Micrometer Prometheus Registry
- springdoc-openapi Swagger UI
- Gradle
- JUnit 5
- k6

## 도메인

현재 도메인은 `Course`, `Student`, `Enrollment`로 구성되어 있습니다.

- `Course`: 강의명, 정원, 현재 신청 인원, 버전, 생성/수정 시각을 관리합니다.
- `Student`: 학생 ID와 이름을 관리합니다.
- `Enrollment`: 학생과 강의의 수강신청 관계를 관리합니다.

주요 무결성 조건은 다음과 같습니다.

- 강의의 `enrolledCount`는 `capacity`를 초과하면 안 됩니다.
- 한 학생은 같은 강의에 한 번만 신청할 수 있습니다.
- `Enrollment` 행 수와 `Course.enrolledCount`는 일치해야 합니다.
- 동시 요청 상황에서도 성공한 수강신청 수는 정원 이하여야 합니다.

## 현재 구현 상태

현재 기본 수강신청 전략은 `no-lock`입니다. 이 전략은 명시적인 락 없이 강의 조회, 중복 확인, 정원 확인, 카운트 증가, 수강신청 저장을 처리합니다.

`no-lock`은 기준선 실험용 구현입니다. 순차 요청에서는 정상 동작하지만, 높은 동시성 상황에서는 정원 초과 성공이나 `enrolledCount`와 실제 신청 행 수의 불일치가 발생할 수 있습니다. 이후 Atomic Update, Pessimistic Lock, Optimistic Lock, Redis Lock 같은 전략을 추가해 결과를 비교하는 구조입니다.

## 패키지 구조

```text
src/main/java/com/example/course_lock_lab
+-- course
|   +-- controller
|   +-- dto
|   +-- entity
|   +-- repository
|   +-- service
+-- enrollment
|   +-- controller
|   +-- dto
|   +-- entity
|   +-- repository
|   +-- service
+-- student
|   +-- entity
|   +-- repository
+-- global
    +-- apiPayload
    +-- config
```

도메인별로 패키지를 먼저 나누고, 각 도메인 내부에서 역할별 패키지를 분리하는 구조입니다.

## API

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/courses` | 강의 생성 |
| `GET` | `/courses` | 강의 목록 조회 |
| `POST` | `/courses/{courseId}/enroll` | 수강신청 |
| `DELETE` | `/courses/{courseId}/enroll?studentId={studentId}` | 수강신청 취소 |
| `GET` | `/students/{studentId}/enrollments` | 학생별 수강신청 내역 조회 |

수강신청 요청 예시는 다음과 같습니다.

```json
{
  "studentId": 1
}
```

## 실행 전 준비

애플리케이션은 MySQL 연결 정보를 환경 변수로 받습니다.

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/course_lock_lab"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "password"
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
```

MySQL에는 `course_lock_lab` 데이터베이스가 필요합니다. 스키마는 Flyway가 `src/main/resources/db/migration/V1__init_schema.sql`을 통해 생성합니다.

## 애플리케이션 실행

```powershell
.\gradlew.bat bootRun
```

기본 포트는 `8080`입니다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health Check: `http://localhost:8080/actuator/health`
- Prometheus Metrics: `http://localhost:8080/actuator/prometheus`

## 테스트

```powershell
.\gradlew.bat test
```

현재 테스트는 No Lock 수강신청의 기본 동작을 검증합니다.

- 수강신청 성공 시 `Enrollment` 생성 및 `enrolledCount` 증가
- 동일 학생의 중복 신청 거절
- 정원 초과 신청 거절
- 수강신청 취소 시 `Enrollment` 삭제 및 `enrolledCount` 감소

## k6 부하 테스트

No Lock 기준선 부하 테스트 스크립트는 `load-tests/k6/no-lock-baseline.js`에 있습니다.

```powershell
k6 run .\load-tests\k6\no-lock-baseline.js
```

현재 셸에서 `k6`가 PATH에 없으면 설치 경로를 직접 지정할 수 있습니다.

```powershell
& "C:\Program Files\k6\k6.exe" run .\load-tests\k6\no-lock-baseline.js
```

기본 조건은 다음과 같습니다.

- 강의 정원: `30`
- 학생 수: `1000`
- 대상 강의: 테스트 시작 시 새로 생성되는 인기 강의 1개
- 전략 태그: `no-lock`

환경 변수로 조건을 바꿀 수 있습니다.

```powershell
$env:BASE_URL = "http://localhost:8080"
$env:CAPACITY = "30"
$env:STUDENTS = "1000"
k6 run .\load-tests\k6\no-lock-baseline.js
```

중요하게 볼 지표는 다음과 같습니다.

- 성공한 수강신청 수가 정원 30을 초과하는지
- `Course.enrolledCount`와 실제 `Enrollment` 행 수가 일치하는지
- 중복 신청 실패가 예상 범위인지
- 예상하지 못한 실패가 있는지
- `enroll_duration`의 p95, p99 응답 시간이 어떤지

## 모니터링

Actuator와 Prometheus 엔드포인트가 활성화되어 있습니다.

수강신청 관련 커스텀 메트릭은 다음 태그를 사용합니다.

- `strategy`: `no-lock` 등 동시성 제어 전략
- `result`: `success`, `fail`
- `reason`: `capacity_full`, `duplicate` 등 실패 사유
- `courseId`: 대상 강의 ID

현재 주요 메트릭은 다음과 같습니다.

- `course.enrollment.requests`
- `course.enrollment.duration`

## 향후 로드맵

1. 애플리케이션과 의존 서비스의 Health 상태를 `UP`으로 확인합니다.
2. k6로 No Lock 기준선 부하 테스트를 실행합니다.
3. 성공 수, 중복 수, 예상하지 못한 실패 수, p95/p99 응답 시간, `enrolledCount`, 실제 신청 행 수를 기록합니다.
4. MySQL, Redis, Prometheus, Grafana용 Docker Compose를 추가합니다.
5. No Lock 기준선 확보 후 Atomic Update 전략을 구현합니다.
6. Pessimistic Lock, Optimistic Lock, Redis Lock 전략을 순차적으로 추가합니다.
7. 전략별 처리량, 지연 시간, 실패율, 일관성 보장 여부를 비교합니다.
