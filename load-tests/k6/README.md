# k6 부하 테스트

## No Lock 기준선 테스트

Spring Boot 애플리케이션을 먼저 `8080` 포트로 실행한 뒤 아래 명령을 실행한다.

```powershell
k6 run .\load-tests\k6\no-lock-baseline.js
```

기본 시나리오:

- 강의 정원: `30`
- 학생 수: `1000`
- 대상 강의: 테스트 시작 시 새로 생성한 강의 1개
- 전략 태그: `no-lock`

환경변수로 기본값을 바꿀 수 있다.

```powershell
$env:BASE_URL = "http://localhost:8080"
$env:CAPACITY = "30"
$env:STUDENTS = "1000"
k6 run .\load-tests\k6\no-lock-baseline.js
```

주요 지표:

- `enroll_success_total`: 성공한 수강신청 수. 이 값이 `30`을 초과하면 No Lock에서 정원 초과 성공이 발생한 것이다.
- `enroll_capacity_full_total`: 정원 초과로 거절된 요청 수.
- `enroll_duplicate_total`: 중복 신청으로 거절된 요청 수. 이 시나리오는 학생마다 1번만 요청하므로 정상적으로는 `0`이어야 한다.
- `enroll_unexpected_failure_total`: 예상하지 못한 실패 수. 정상적으로는 `0`이어야 한다.
- `enroll_duration`: 수강신청 API만 대상으로 측정한 응답 시간. `p(95)`, `p(99)`를 기준선 응답 시간으로 기록한다.

스크립트는 종료 시 아래 검증값을 추가로 출력한다.

- `course.enrolledCount`
- `enrollment row count inferred from student APIs`
- `enrolledCount exceeds capacity`
- `enrolledCount and enrollment row count mismatch`

`http_req_duration`은 setup/teardown 요청까지 포함한다. 수강신청 응답 시간 비교에는 `enroll_duration`을 우선 사용한다.
