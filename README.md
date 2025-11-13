## <img width="1200" height="1102" alt="image" src="https://github.com/user-attachments/assets/be96839f-1691-4b3f-87b8-43eab7925c3c" />
---

> **E-Sports 베팅 플랫폼**
> ---

## 📋 목차
🎮 [프로젝트 소개](#-프로젝트-소개)

⭐ [핵심 기능](#-핵심-기능)

⚙️ [프로젝트 설계](#-프로젝트-설계)

🛠️ [기술 스택](#-기술-스택)

🤔 [기술적 의사결정](#-기술적-의사결정)

📈 [성능 개선](#-성능-개선)

🚨 [트러블 슈팅](#-트러블-슈팅)

🧑‍🤝‍🧑 [팀원 소개](#-팀원-소개)

---

## 🎮 프로젝트 소개
**OddVenture — E-Sports 베팅 플랫폼**
> 🌱 개발 배경 및 문제 인식

- e스포츠를 보는 팬들은 단순한 관람보다 **내 선택이 맞았는지 확인하는 순간**에 더 크게 몰입합니다.
- 그러나 지금의 시청 경험은 그 긴장과 기대를 오래 이어갈 장치가 부족합니다. 커뮤니티에서 예측과 토론은 활발하지만, 선택이 기록되거나 보상으로 이어지지 않으니 흥미는 금세 식고, 팬들의 관심은 쌓이지 못한 채 흩어집니다.
- 결과적으로 경기는 흘러가고, 팬들은 **경기에 직접 참여했다는 감각**을 충분히 얻지 못한 채 관람자로만 머물게 됩니다.

> 🎯 솔루션: Oddventure - E-sports 베팅 플랫폼

- Oddventure는 **팬들의 선택과 몰입을 기록하고 보상하는 참여형 플랫폼**입니다.
- Oddventure를 통해 팬들은 단순히 경기를 관람하는 것을 넘어, 자신이 한 선택이 기록되고 결과로 검증되는 경험을 얻습니다.
- 예측이 맞았을 때는 성취감과 보상을 통해 더 큰 재미를 느낄 수 있고, 커뮤니티 안에서 다른 팬들과 함께 토론하며 자신의 위치를 확인하는 즐거움도 누릴 수 있습니다.
- 이렇게 쌓인 참여와 기록은 팬들에게 “**내가 경기의 일부였다**”는 감각을 제공하며, 시청 경험을 단순한 관람에서 참여형 경험으로 확장시킵니다.

> 🤝 핵심 가치 제안

- **가벼운 진입**: 현금이 아닌 게임 포인트로 누구나 부담 없이 참여
- **명확한 보상**: 결과에 따라 배당 포인트를 즉시 정산
- **선택의 기록**: 나의 예측과 결과가 남아 개인적 만족감 강화
- **몰입도 상승**: 베팅을 통한 참여로 경기의 흥미와 몰입 증대

> 🙋 타겟 사용자

- **20-30대 직장인**: 바쁜 일상 속에서 간단하게 재미를 느끼고 싶은 사람
- **E-Sports 팬**: 단순히 보는 것을 넘어 직접 참여하는 재미를 느끼고 싶은 사람
- **관련 사업자/운영자**: 새로운 수익 창출 모델 및 이벤트가 필요한 사람

---

## ⭐ 핵심 기능
<details>
<summary><b>👤 사용자 관리</b></summary>

### 회원가입 및 로그인
- 이메일/비밀번호 기반 일반 회원가입
- JWT 기반 인증 시스템

### 계정 관리
- 이메일 변경 (기존 이메일 확인 후)
- 비밀번호 변경 (기존 비밀번호 확인 후)
- 계정 탈퇴 기능
- 토큰 갱신 (Refresh Token)

### 프로필 관리
- 개인 프로필 조회 및 수정
- 닉네임, 이메일, 포인트 관리
- 타 사용자 프로필 조회 기능
    
### 사용자 검색
- 사용자 id 기반 사용자 검색
- 커서 기반 페이지네이션
- Elasticsearch 기반 고성능 검색
- 이메일, 닉네임, 자기소개 통합 검색
    
### 권한 관리
- 사용자 권한 설정 (USER, ADMIN)
- 관리자를 통한 권한 변경
- 권한별 기능 접근 제어
    
### 캐싱 및 성능 최적화
- 프로필 검색 결과 캐싱
- 통계용 사용자 데이터 캐싱
- 스케줄러를 통한 주기적 캐시 갱신
- 검색 성능 최적화

</details>

<details>
<summary><b>🎯 매치 시스템</b></summary>

### 매치 생성 및 관리
- 스케줄러를 통한 실제 매치 정보 연동
- 트래픽이 적은 새벽시간대를 활용
- 배치를 통한 안정성 확보
- bulk insert 기반 대용량 데이터 삽입
- 매치 정보 수정
    
### 매치 라이프 사이클
- 3단계 상태 관리 (예정 → 진행 → 종료)
- 매치 시작 시 자동 상태값 변화 (진행)
  - 실시간 베팅 중단 알람 전송
- 실제 매치 결과 연동 및 상태값 변화 (종료)
  - 해당 매치의 베팅 정산

### 배당률 관리
- 팀당 베팅 총액 기반 실시간 배당률 계산
- Redisson 분산 락 기반 동시성 제어

### 매치 검색 및 조회
- ElasticSearch 기반 검색
- 오타 허용
- 다양한 조건별 검색 (이름, 팀 이름, 기간 등)
- 상세 매치 조회
</details>

<details>
<summary><b>🎮 베팅 시스템</b></summary>

### 베팅 생성 및 관리
- 베팅 가능 여부 확인 후 생성
  - 포인트 잔액
  - 매치 상태
- 실시간 배당률 적용
- 배당률 변경 시 알람을 통한 새로고침 요청
  - Redis Pub/Sub 활용
    
### 베팅 취소
  - 취소 가능 여부 확인 후 취소
  - 포인트 환불
  - 변경된 사항 실시간 적용
  - Redisson 기반 분산 락 적용

### 베팅 이력 조회
- 페이지네이션 기반 베팅 이력 조회

### 정산 및 배당금 지급
- 매치 종료 후 일괄 지급
- 베팅 생성 시점 배당률 적용
- 스케줄링 및 배치 적용
</details>

<details>
<summary><b>👍 인기검색어 시스템</b></summary>

### Redis ZSet 
- 메모리 저장으로 인한 빠른 조회
- 검색어 자동 등록 및 순위 나열
- 중복 제거를 통한 데이터 꼬임 방지
    
### 캐싱 및 성능 최적화
- 매치 검색 결과 캐싱
- 새로운 키워드로 검색 시 캐시 갱신

### 스케줄링
- Write-Back
- DB 와 연동
- 데이터 원자성 & 신뢰성 보장
- 인기 검색어 갱신
</details>

<details>
<summary><b>🤖 ai 시스템</b></summary>

### 챗봇
- LLM 모델 적용
- 사용자와의 대화 기억
- ai-agent를 활용한 자발적 답변 제공
- tool 생성을 통해 ai의 사고 지원
- 효율적인 답변 기능 (CoT)
- 단계적 사고를 위한 흐름 구현 (ReAct)
    
### 경기 승률 예측
- 데이터를 기반해 도출한 승률 및 예측 제공
- 일관된 답변을 위한 프롬프트 생성

</details>

<details>
<summary><b>📊 모니터링 & 시각화</b></summary>

### 모니터링
- 메트릭 수집을 위한 Prometheus 도입
- 시각화를 위한 Grafana 도입
- 도커 컨테이너를 통해 동작
- 대시보드를 통한 장애 모니터링 및 성능 체크

</details>

---
## ⚙️ 프로젝트 설계

> **🎨 와이어 프레임**
<img width="1895" height="736" alt="image" src="https://github.com/user-attachments/assets/ea5b9cd6-dda0-4f3b-9e14-3b18a8529027" />


> **🗂️ ERD**
<img width="1340" height="635" alt="image" src="https://github.com/user-attachments/assets/f5988cac-85f2-447b-84c4-27a9281b3441" />



> **🏗️ Service Architecture**
<img width="1344" height="1532" alt="image" src="https://github.com/user-attachments/assets/158039e3-c26d-443c-aafa-1e82562ee579" />


> **🚀 서비스 플로우**
<img width="983" height="1079" alt="image" src="https://github.com/user-attachments/assets/65c7cd3b-8a8a-4fdb-b878-f0b94a4b5eee" />
<img width="1004" height="564" alt="image" src="https://github.com/user-attachments/assets/d8739d82-0b47-4a40-9693-fcfc17823f1a" />



> 📚 API 명세서

📄 **[API Documentation](https://www.notion.so/teamsparta/S-A-2872dc3ef51481fd9864dba486a8bfb2)**

---
## 🛠️ 기술 스택

### Language

![Java 17](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

### Backend

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring%20Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Validation](https://img.shields.io/badge/Spring%20Validation-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Database

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2%20Database-464646?style=for-the-badge&logo=h2&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge&logo=querydsl&logoColor=white)

### Search

![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Kibana](https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white)

### Cache

![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### Queue

![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

### Security

![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

### Cloud Service

![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![AWS ECR](https://img.shields.io/badge/AWS%20ECR-FF9900?style=for-the-badge&logo=amazonelasticcontainerregistry&logoColor=white)
![Grafana Cloud](https://img.shields.io/badge/Grafana%20Cloud-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Amazon Prometheus](https://img.shields.io/badge/Amazon%20Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![AWS Parameter Store](https://img.shields.io/badge/AWS%20Parameter%20Store-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)

### CI/CD

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

### Docs

![REST Docs](https://img.shields.io/badge/Spring%20REST%20Docs-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Asciidoctor](https://img.shields.io/badge/Asciidoctor-E4004B?style=for-the-badge&logo=asciidoctor&logoColor=white)

### Monitoring

![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Spring Actuator](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Test

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-D43A2B?style=for-the-badge&logo=mockito&logoColor=white)
![K6](https://img.shields.io/badge/K6-8C5FCD?style=for-the-badge&logo=k6&logoColor=white)
![Jacoco](https://img.shields.io/badge/Jacoco-C32A2B?style=for-the-badge&logo=jacoco&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

### Collaboration

![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Zep](https://img.shields.io/badge/Zep-7B61FF?style=for-the-badge&logo=zep&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![ERD Cloud](https://img.shields.io/badge/ERD%20Cloud-1E90FF?style=for-the-badge&logoColor=white)

### IDE

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white)

### Open API

![Grid GG](https://img.shields.io/badge/Grid%20GG-1E90FF?style=for-the-badge&logoColor=white)
![Groq](https://img.shields.io/badge/Groq-00C980?style=for-the-badge&logo=groq&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Library

![Lombok](https://img.shields.io/badge/Lombok-DC382D?style=for-the-badge&logo=lombok&logoColor=white)
![Jackson](https://img.shields.io/badge/Jackson-C50F2C?style=for-the-badge&logo=jackson&logoColor=white)
![WebClient](https://img.shields.io/badge/WebClient-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![LangGraph4j](https://img.shields.io/badge/LangGraph4j-8A2BE2?style=for-the-badge&logoColor=white)

---

## 🤔 기술적 의사결정

<details>
<summary><b>JWT / Spring Security</b></summary>

## 1. 도입 배경

<aside>

Oddventure는 사용자 인증이 필요한 API가 많고,

챗봇, 실시간 알림, 관리자 기능 등 역할별 접근 제어가 필요한 구조였다.

또한 시스템 전반이 Redis Pub/Sub과 WebSocket 기반의 실시간 흐름으로 구성되어 있어

서버가 세션 정보를 유지하는 방식은 확장성과 유지보수 측면에서 부담이 컸다.

이런 요구사항을 충족하기 위해 서버 상태를 보관하지 않는

Stateless 인증 방식인 JWT 기반 구조를 도입하게 되었다.

**요구사항**

- 프론트엔드·백엔드 분리 환경에서 확장성 있는 인증 구조
- 서버 증설 시 세션 공유 없이 바로 확장 가능할 것
- 관리자/일반 사용자 역할 분리 및 접근 제어 지원

</aside>

## 2. 기술 선택 과정

<aside>

**JWT 선택 이유**

- 서버 상태를 저장하지 않는 Stateless 방식으로 수평 확장이 용이
- WebSocket Handshake 과정에서도 토큰 검증을 동일하게 적용할 수 있음
- API 중심 SPA 환경에 적합하며 구현 복잡도가 낮음
- Redis Pub/Sub의 userId 기반 채널 구성과 자연스럽게 연결됨

**Spring Security 선택 이유**

- 인증/인가 정책을 한 계층에서 일관되게 관리할 수 있음
- JWT 토큰 검증 필터, URL 권한 정책을 체계적으로 적용 가능
- 실무 표준 기술로 유지보수와 확장에 유리
- 관리자 API와 일반 사용자 API의 권한 분리를 명확하게 설정 가능

</aside>

## 3. 현재 적용 방식

<aside>

- 로그인 시 access/refresh token 발급
- refresh token 쿠키와 Redis에 저장
- 모든 API 요청에서 JWT 필터를 통한 인증 처리
- 관리자 권한(Role)을 분리하여 중요 API 접근 통제
- Spring Security 필터 체인을 통해 인증·권한 로직을 일원화

- 실제 트래픽 시뮬레이션 기반의 성능 측정 필요
- 기존 Grafana + Prometheus 모니터링 스택과의 통합 필요

</aside>

</details>

<details>
<summary><b>Redis</b></summary>

## 1. 도입 배경

<aside>

oddventure는 E-sports 베팅 플랫폼으로, 대량의 동시 조회 트래픽 (경기 상세, 배당률 확인)과 데이터 정합성이 매우 중요한 동시 쓰기 트래픽 (베팅 실행, 포인트 차감)이 동시에 발생하는 서비스이다.

MySQL에만 의존하는 초기 아키텍처는 병목 현상을 보였고, DB의 부하를 줄여 API 응답 속도를 개선하고, 분산 환경에서도 안정적인 동시성 제어와 데이터 정합성을 보장하기 위해 도입하였다. 

</aside>

## 2. 기술적 요구사항

<aside>

단순한 DB 부하 분산을 넘어, `oddventure`는 여러 도메인에서 발생하는 복합적인 문제를 해결할 솔루션이 필요했다.

- **캐싱 (Caching):** TeamService처럼 데이터 변경이 거의 없는 정적 데이터와, MatchService의 FINISHED 상태 경기 데이터를 DB 대신 빠르게 조회할 중앙 저장소가 필요했다.
- **동시성 제어 (Locking):** 베팅이나 포인트처럼 동시에 실행되면 데이터가 꼬이는 위험한 쓰기(Write) 작업을, DB에 락을 걸지 않고 제어할 분산 락 메커니즘이 필요했다.
- **원자적 연산 (Atomic Counters):** MatchService의 incrementViewCount처럼 병목을 유발하는 DB UPDATE를 대체할 Write-Behind 패턴과, 1ms 이내에 완료되는 고속 카운터가 필요했다.
- **실시간 랭킹 (Ranking):** HotKeywordsService처럼 실시간 인기 검색어 순위를 DB ORDER BY 없이 빠르게 집계할 특수 자료구조가 필요했다.
- **토큰 저장 (Session Store):** AuthService의 RefreshToken처럼 만료 시간이 있는 인증 데이터를 안정적으로 관리할 Key-Value 저장소가 필요했다.
- **메시지 발행/구독 (Pub/Sub):** BetService에서 발생한 배당률 변경 이벤트를 WebSocket으로 실시간 전파하기 위한 메시지 브로커가 필요했다.
</aside>

## 3. 의사결정 과정

<aside>

위 요구사항을 해결하기 위해 세 가지 방안을 검토

**1. DB 최적화 (MySQL Only):**

- **내용:** QueryDSL을 최적화하고 인덱스를 추가하며, DB의 비관적 락을 사용
- **한계:** k6 부하 테스트 결과, 쿼리 속도가 문제가 아니라 DB 커넥션 풀 고갈이 병목. 비관적 락은 이 병목을 더욱 심화시켜 100% 장애를 유발

**2. 로컬 캐시 (Caffeine, Ehcache):**

- **내용:** Spring Boot 애플리케이션의 힙 메모리에 캐시
- **한계:** 네트워크 오버헤드가 없어 가장 빠르지만, 분산 환경에서 치명적인 데이터 불일치 문제를 유발. AWS EC2로 서버가 2대 이상 증설되면, 1번 서버의 캐시와 2번 서버의 캐시가 달라짐. 또한 캐싱 외의 요구사항(분산 락, 랭킹, Pub/Sub)을 해결할 수 없음

**3. 글로벌 캐시 / 데이터 스토어 (Redis):**

- **내용:** 외부 In-Memory 서버에 데이터를 중앙 집중화
- **장점**
    - In-Memory 기반으로 DB보다 압도적으로 빠름.
    - 중앙 저장소이므로 분산 환경에서 데이터 일관성을 보장.
    - 캐싱뿐만 아니라, `Redisson`을 통한 분산 락, `ZSet`(랭킹), `INCR`(카운터), `Key-Value`(토큰 저장), `Pub/Sub` 등 6가지 기술적 요구사항을 하나의 기술 스택으로 모두 해결할 수 있
</aside>

## 4. 최종 선택 이유

<aside>

`Redis`는 단순 캐시가 아니라, `oddventure`의 6가지 핵심 문제를 동시에 해결하는 Multi-Purpose 데이터 스토어 역할을 수행하기 때문에 최종 채택하였다.

1. **Read-Through 캐시 (**`@Cacheable`**):** `TeamService`와 `MatchService`의 `getMatch` API에 적용하여 `SELECT` 병목을 해결
2. **분산 락 (**`Redisson`): `BetService`와 `UserService`에 `RLock` 및 `RMultiLock`을 적용하여, DB 비관적 락으로 인한 커넥션 풀 고갈을 해결하고 데이터 정합성을 확보
3. **원자적 카운터 (Write-Behind):** `MatchService`의 `incrementViewCount`를 DB `UPDATE`에서 Redis `INCR`로 변경하여 `UPDATE` 병목을 해결
4. **실시간 랭킹 (**`ZSet`**):** `HotKeywordsService`가 `ZINCRBY` (`incrementScore`)를 사용해 DB 쿼리 없이 실시간 인기 검색어 랭킹을 구현
5. **인증 토큰 저장:** `AuthService`가 `RefreshToken`을 Redis의 `Key-Value`에 저장하여 빠르고 안정적인 인증을 구현
6. **Pub/Sub** (메시지 발행): `BetService`가 베팅 성공 시 배당률 변경 이벤트를 Redis `PUBLISH`로 발행하여, `RedisSubscriber`가 WebSocket으로 클라이언트에 실시간 전파

이처럼 하나의 기술 스택(Redis)을 도입하여 캐싱, 분산 락, 랭킹, 카운터, 토큰 저장, Pub/Sub라는 6가지의 복합적인 문제를 모두 해결함으로써, 인프라 복잡도를 낮추고 시스템 전반의 성능과 안정성을 극대화했다.

</aside>

</details>

<details>
<summary><b>Redis Pub/Sub</b></summary>

## 1. 도입 배경

<aside>

Oddventure 프로젝트는 실시간성이 중요한 기능이 두 가지 존재합니다.

* **AI 챗봇 메시지 흐름**
    * 사용자가 입력한 메시지를 AI가 처리하고 다시 응답을 전달해야 합니다.
    * 이 과정은 동기 API 호출보다 비동기 처리가 적합했고, 여러 컴포넌트가 메시지를 빠르게 주고받아야 했습니다.
* **실시간 알림 (배당률 변경, 경기 상태 변화 등)**
    * 경기 중 데이터는 수시로 바뀌며, 여러 사용자에게 즉각 전송해야 합니다.
    * 단순 저장 후 조회가 아니라 발생 즉시 전달이 필요했습니다.

즉, 시스템 전반에서 지연 시간이 짧고, 비동기 메시징 기반의 통신이 필요해졌고 이를 안정적으로 처리할 메시지 브로커가 필요했습니다.

</aside>

## 2. 기술적 요구사항

<aside>

메시징 도입 당시 고려한 요구사항은 다음과 같습니다.

* 지연 시간이 매우 짧아야 함 (AI 챗봇 응답, 실시간 알림)
* 메시지 구조가 단순함 (챗봇의 input/output, 경기 알림 이벤트)
* 배포 환경에서 추가 인프라 부담이 적을 것
* 간단하게 확장 가능할 것 (채널 증가, 구독자 증가)
* 로컬 개발 환경에서도 쉽게 테스트 가능할 것

</aside>

## 3. 의사결정 과정

<aside>

메시징 선택지로는 보통 다음을 비교했습니다.

| 기술 | 장점 | 단점 |
| --- | --- | --- |
| **Redis Pub/Sub** | 지연 낮음, 설정 간단, 트래픽 처리 우수, 로컬 개발 쉬움 | 메시지 영속성이 없음 |
| **RabbitMQ** | 메시지 영속성, 라우팅 기능 우수, 안정적임 | 인프라 구성 복잡, 오버엔지니어링 가능 |
| **Kafka** | 고성능 스트리밍, 대규모 분산 메시징 | 운영/학습 비용 높음, 현재 규모에는 과함 |

프로젝트 특성을 기준으로 판단했을 때:

* 챗봇 메시지는 요청 → 응답 흐름이 매우 빠르게 돌아야 하며
* 경기 알림 또한 **"저장 목적"이 아니라 "즉시 전달"**이 핵심이었습니다.
* 메시지를 저장해둘 필요는 없고,
* 복잡한 라우팅 체계도 필요 없었습니다.

따라서 오버엔지니어링을 피하면서 실시간 성능을 확보하기에 Redis Pub/Sub이 가장 적합하다고 판단했습니다.

</aside>

## 4. 최종 선택 이유

<aside>

* **낮은 지연 시간:** 지연 시간이 매우 낮고, 실시간성에 적합합니다.
* **단순한 구현/운영:** 구현/운영이 가장 단순하여 챗봇과 알림 기능 모두 빠르게 구축 가능했습니다.
* **인프라 재활용:** 이미 Redis를 캐싱 용도로 사용 중이어서 추가 인프라 구성 없이 재활용 가능했습니다.
* **쉬운 확장성:** 스케일아웃이 쉬워 구독자가 늘어도 채널만 추가하면 됩니다.
* **Fit:** 실시간 메시지에 한정할 때 RabbitMQ보다 가볍고 필요한 기능에 정확히 맞았습니다.

</aside>

## 5. 현재 적용 방식

<aside>

* **AI 챗봇**
    * `chat:{userId}:input` → 사용자 메시지 전달
    * `chat:{userId}:output` → AI 응답 전달
    * WebSocket과 연결해 최종적으로 클라이언트에 실시간 업데이트
* **실시간 알림**
    * 경기 상태 변경, 배당률 변경 등을 채널로 발행
    * 관련 사용자 또는 관리자 화면에 즉시 반영

</aside>

</details>


<details>
<summary><b>Redisson</b></summary>

## 1. 도입 배경

<aside>

oddventure의 핵심 기능인 베팅 및 포인트 지급 API는 여러 사용자의 동시 요청에 매우 민감합니다. 서버가 여러 대로 증설되는 분산 환경에서도 데이터 정합성을 100% 보장하면서, DB 커넥션 풀을 고갈시키지 않는 안정적인 동시성 제어 메커니즘을 구축하기 위해 도입했습니다.

</aside>

## 2. 기술적 요구사항

<aside>

단순한 `synchronized` 블록으로는 분산 환경의 동시성 문제를 해결할 수 없으므로, 다음과 같은 요구사항을 정의했습니다.

* **분산 환경 보장 (Distributed):** 여러 Spring Boot 서버(EC2 인스턴스) 간에 동일한 락(Lock)을 공유해야 합니다.
* **안정성 및 성능 (Stability & Performance):** DB 커넥션 풀을 점유하는 방식(비관적 락)은 시스템 전체 장애를 유발하므로 절대 사용해선 안 됩니다. 락(Lock)은 DB가 아닌 별도의 저장소에서 매우 빠르게 처리되어야 합니다.
* **다중키 락 (Multi-Lock):** `BetService.createBet`은 '유저 포인트'와 '매치 베팅액'이라는 두 개의 자원을 동시에 잠가야 합니다(Deadlock 방지).
* **타임아웃 및 예외 처리:** 락 획득을 무한정 대기하다 시스템이 다운되는 것을 막고, 락 획득 실패 시(예: 10초) "현재 처리 중"이라는 명확한 예외(HTTP 409 Conflict)를 반환해야 합니다.

</aside>

## 3. 의사결정 과정

<aside>

1.  **비관적 락 (Pessimistic Lock - `SELECT ... FOR UPDATE`):**
    * JPA의 `@Lock` 어노테이션으로 구현이 간단하고 정합성을 강력하게 보장합니다.
    * **한계:** k6 부하 테스트에서 `MatchService`의 `UPDATE` 쿼리가 DB 커넥션 풀(10개)을 고갈시켜 100% 장애를 유발하는 것을 이미 확인했습니다. 비관적 락은 이와 동일하게 DB 커넥션을 점유하므로, `BetService`에 적용 시 동일한 병목 현상을 유발할 것이 뻔하여 탈락시켰습니다.

2.  **낙관적 락 (Optimistic Lock - `@Version`):**
    * DB 커넥션을 점유하지 않아 성능 저하가 없습니다.
    * **한계:** 베팅처럼 충돌이 빈번한 요청에는 부적합합니다. 100명이 동시에 베팅하면 1명만 성공하고 99명은 `OptimisticLockException` 예외를 받게 됩니다. 이는 사용자 경험이 매우 나쁘고, 애플리케이션 레벨의 복잡한 재시도 로직을 요구하므로 탈락시켰습니다.

3.  **분산 락 (Distributed Lock - Redis `SETNX`):**
    * DB가 아닌 In-Memory 저장소(Redis)에서 락을 관리하므로, DB 커넥션 병목 문제를 원천적으로 해결합니다.
    * **한계:** `SETNX`와 `Expire`를 이용해 직접 구현할 경우, 락 갱신이나 서버 다운 시 락 자동 해제 등 엣지 케이스 처리가 매우 복잡하고 위험합니다.

4.  **분산 락 라이브러리 (Redisson):**
    * Redisson은 Redis 기반 분산 락의 모든 복잡한 문제(Lease Time, Deadlock 방지)를 해결해주는 검증된 Java 라이브러리입니다.
    * `RLock` 인터페이스는 Java의 `ReentrantLock`과 사용법이 거의 동일합니다.
    * `tryLock(10, 5, TimeUnit.SECONDS)`을 통해 락 획득 타임아웃 (요구사항 4번)을 손쉽게 구현할 수 있습니다.
    * `RMultiLock`을 지원하여 다중키 락 (요구사항 3번)을 원자적으로 처리할 수 있습니다.

</aside>

## 4. 최종 선택 이유

<aside>

Redisson은 **비관적 락의 DB 커넥션 고갈 문제**와 **낙관적 락의 잦은 충돌 문제**를 동시에 해결하는 유일한 방안이었습니다.

**구현 흐름 :** Redisson을 도입하며 Spring AOP의 '셀프 호출' 문제를 피하기 위해 서비스를 물리적으로 분리했습니다.

* **BetService / UserService (역할: 락 매니저):**
    * `RedissonClient`를 주입받아 `RLock` 또는 `RMultiLock`을 생성합니다.
    * `tryLock()`으로 락 획득을 시도하고, 실패 시 `BetException(BET_LOCK_FAILED)` (HTTP 409)를 즉시 반환합니다.
    * 락 획득 성공 시, `BetTransactionService`를 외부 호출하여 트랜잭션을 실행합니다.
    * `finally` 블록에서 `multiLock.unlock()`을 호출하여 락을 안전하게 해제합니다.

* **BetTransactionService / UserPointTransactionService (역할: DB 작업자):**
    * `@Transactional`의 책임을 가지며, 락이 이미 획득된 상태에서만 호출됩니다.
    * DB 비관적 락(`findByIdForUpdate`) 대신 일반 `findById`를 사용하므로 DB 커넥션을 점유하지 않습니다.

</aside>

</details>


<details>
<summary><b>CI/CD</b></summary>

## 1. 도입 배경

<aside>

배포 과정은 항상 반복 작업과 오류 가능성이 있었습니다.

* 로컬/서버 간 환경 차이
* 수동 배포로 인한 휴먼 에러
* 운영자의 서버 직접 SSH 접속으로 인한 불안정성

이로 인해 안정적이고 일관된 배포를 보장하기 어려웠습니다. 배포는 "한 번 성공했다고 끝나는 게 아니라, 누구나 같은 과정으로 안정적으로 실행 가능한 구조"가 필요하다고 판단했습니다.

</aside>

## 2. 의사결정 과정

<aside>

두 가지 주요 대안을 비교 검토했습니다.

**1. Jenkins**
* **장점:**
    * 자유도와 확장성이 매우 뛰어납니다.
    * 오픈소스 표준으로 다양한 플러그인을 제공합니다.
    * 대규모 CI/CD 환경에 적합합니다.
* **한계:**
    * 별도의 서버 구축 및 운영이 필요하여 운영 부담이 큽니다.
    * 플러그인 관리에 대한 부담이 존재합니다.
    * 초기 구축 비용과 유지보수 인력이 필요합니다.

**2. GitHub Actions**
* **장점:**
    * GitHub 저장소와 완벽하게 통합되어 가장 가볍고 단순한 선택지입니다.
    * 저장소와 바로 연동되므로 추가적인 인프라(서버) 구축이 필요 없습니다.
    * Marketplace를 통해 다양한 공식/커뮤니티 액션을 활용할 수 있습니다.
    * 무료 플랜으로도 작은 팀/프로젝트에는 충분히 적합합니다.
* **한계:**
    * 무료 플랜 기준 실행 시간에 제한이 있습니다.

</aside>

## 3. 최종 선택 이유

<aside>

**GitHub Actions**는 **단순성, 운영 효율성, 협업 편의성**을 모두 충족하므로 최종 선택하였습니다.

* Jenkins는 현 프로젝트 규모에 비해 너무 무겁고, 별도 서버 운영 및 플러그인 관리에 드는 부담이 크다고 판단했습니다.
* GitHub Actions는 저장소에 YAML 파일 하나만 추가하면 즉시 CI/CD 파이프라인이 동작하며, 모든 개발자가 배포 과정을 코드로 리뷰하고 관리할 수 있게 해줍니다.
* 결국 GitHub Actions가 현재 프로젝트 상황에 가장 적합한 해법이었습니다.

</aside>

## 4. 현재 적용 방식

<aside>

GitHub Actions의 간단한 YAML 워크플로우를 통해 다음과 같은 파이프라인을 구성했습니다.

1.  **Docker 빌드:** `Dockerfile`을 기반으로 Spring Boot 애플리케이션을 빌드합니다.
2.  **Docker Hub Push:** 빌드된 도커 이미지를 Docker Hub (Private Registry)에 푸시합니다.
3.  **AWS EC2 Pull & Run:** AWS EC2 서버에 SSH로 접속하여, Docker Hub에서 최신 이미지를 Pull 받고 기존 컨테이너를 중지 후 새 컨테이너를 실행합니다.

</aside>

</details>

<details>
<summary><b>Elasticsearch</b></summary>

## 1. 기술 설명

<aside>
Elasticsearch는 분산형 검색 및 분석 엔진으로, 대량의 데이터를 실시간으로 색인하고 빠르게 검색할 수 있습니다.
특히 ngram tokenizer를 사용하면 부분 문자열 단위로 색인을 생성하여 **부분 검색(자동완성, 중간 일치 검색 등)**에 유리합니다.
</aside>

## 2. 기술 장점

<aside>
<ul>
    <li><b>검색 성능 향상:</b> 대량 데이터에서도 실시간 검색이 가능하며, MySQL의 LIKE 연산보다 빠른 응답 속도를 제공합니다.</li>
    <li><b>부분 검색 지원:</b> ngram 분석기를 통해 중간 일치 검색이 가능하여, 사용자 경험 개선 (예: 자동완성, 오타 대응 등).</li>
    <li><b>확장성:</b> 수평 확장이 용이하여 대규모 데이터 검색에도 안정적인 성능 유지.</li>
    <li><b>전문화된 검색 기능:</b> 가중치 부여, 점수 기반 검색 결과 정렬 등 고도화된 검색 기능 확장이 가능합니다.</li>
    <li><b>독립적 검색 서버 운영:</b> 애플리케이션 DB와 검색 서버를 분리하여, 서비스 안정성과 DB 부하 감소에 기여합니다.</li>
</ul>
</aside>

## 3. 단점 / 한계점 / 주의사항

<aside>
<ul>
    <li><b>운영 복잡성:</b> Elasticsearch 클러스터 운영, 모니터링, 백업 관리 등 추가 인프라 관리가 필요합니다.</li>
    <li><b>데이터 동기화 비용:</b> MySQL과 별도로 데이터를 색인해야 하므로, 실시간 동기화 로직이 필요합니다.</li>
    <li><b>학습 비용:</b> Query DSL, 색인 설정, 분석기 등 초기 학습 곡선이 존재합니다.</li>
    <li><b>추가 리소스 소모:</b> 별도의 검색 서버 운영으로 인프라 비용이 증가할 수 있습니다.</li>
    <li><b>과도한 도입 위험:</b> 단순 검색만 필요하다면 오히려 MySQL FullText 검색이나 LIKE 최적화로 충분할 수 있습니다.</li>
</ul>
</aside>

## 4. 도입 배경

<aside>
<p><strong>문제</strong></p>
<p>MySQL 검색의 한계 : <code>LIKE '%keyword%'</code> 검색은 인덱스를 활용하지 못해 대량 데이터에서 모든 row에 대해 CPU 연산을 하므로, 대용량 데이터 조회 시 600ms 이상 걸리는 성능 저하가 발생합니다.</p>
<p><strong>목표</strong></p>
<p><code>LIKE %Keyword%</code> 검색도 가능하면서, 대용량 데이터를 조회하더라도 400ms 내의 응답을 줄 수 있도록 하는 것입니다.</p>
</aside>

## 5. 의사 결정

<aside>
<p><strong>여러 가지 대안</strong></p>
<ul>
    <li>MySQL FULLTEXT parser 방식</li>
</ul>
<p><strong>결정 근거</strong></p>
<p>MySQL FULLTEXT parser 방식을 우선적으로 사용하여 <code>LIKE %Keyword%</code> 검색 조회가 가능하게 되었지만, 대용량 데이터 조회 시 응답 시간이 일정하지 않고, 인덱스 토큰 생성 비용이 크고, 최대 응답 시간이 MySQL <code>LIKE %Keyword%</code> 사용했을 때와 큰 차이가 없었습니다. 그에 비해 elasticsearch는 <code>LIKE %Keyword%</code> 검색 조회도 가능하고, n-gram 방식을 사용하면 역시 인덱스 토큰 생성 비용이 크지만 다른 대안이 존재하며, 대용량 데이터 <code>LIKE %Keyword%</code> 조회 시에도 400ms 내의 응답이 가능하여 도입하게 되었습니다.</p>
</aside>

## 6. 작업 계획

<aside>
<p>Elasticsearch 기본 설정 → Document n-gram 설정 → Elasticsearch repository로 인덱스 색인 → <code>LIKE %Keyword%</code> 검색 기능 구현</p>
</aside>

</details>

<details>
<summary><b>Spring Batch</b></summary>

## 1. 도입 배경

<aside>

매치 종료 시 각 유저의 베팅 결과에 따라 포인트를 정산해야 합니다.
매치 종료는 매일 주기적으로 발생하며, 베팅 데이터 역시 대량이므로 정합성과 안정적인 대용량 처리가 중요합니다.
또한 정산 로직은 “한 번의 대량 데이터 처리 → 포인트 지급”이라는 명확한 배치(Batch) 성격을 가집니다.

</aside>

## 2. 기술적 요구사항

<aside>

* **대량 데이터 처리 안정성:** 수많은 베팅 데이터를 한 번에 처리하므로, Chunk 기반의 안정적인 커밋이 필요합니다.
* **정합성 보장:** 포인트 지급의 중복, 누락, 재처리 문제를 예방해야 합니다.
* **재시작 가능성:** 배치 실행 도중 장애 발생 시, 중단 지점부터 안정적으로 재처리 가능해야 합니다.
* **트랜잭션 관리:** 베팅·정산 로직이 한 트랜잭션 내에서 명확하게 커밋되도록 관리할 필요가 있습니다.

</aside>

## 3. 의사결정 과정

<aside>

1.  **단순 Spring Scheduler**
    * 스케줄러는 “작업 실행 시점 제어”에는 적합하지만, 대량 데이터 처리의 Chunk 처리, 트랜잭션 경계, 재시작 구조가 없습니다.
    * 장애 발생 시 전체 로직이 다시 실행되어 중복 지급 위험이 존재합니다.

2.  **메시지 큐 (RabbitMQ/Kafka) 기반 비동기 처리**
    * 실시간성은 높지만, 정산 기능에는 불필요한 스펙입니다.
    * 대량의 데이터를 한 번에 처리하기보다는, 이벤트 발생 시마다 처리하는 방식이므로 메모리 관리가 비효율적일 수 있습니다.

</aside>

## 4. 최종 선택 이유

<aside>

1.  **Chunk-Oriented Processing**
    * Spring Batch는 대규모 데이터를 메모리를 과도하게 쓰지 않고 Chunk 단위로 읽고(Read) 처리(Process)한 뒤 한 번에 쓰기(Write) 때문에 성능과 안정성을 동시에 확보할 수 있습니다.
    * 

2.  **Restartability (재시작성)**
    * `ExecutionContext`를 기반으로 마지막 처리 위치를 저장해, 장애가 발생해도 중단 지점부터 다시 재실행할 수 있습니다.
    * 이를 통해 포인트 중복 지급과 같은 심각한 데이터 정합성 문제를 원천적으로 방지합니다.

3.  **운영 편의성**
    * `JobRepository`가 자동으로 실행 상태, 실행 파라미터, 처리 건수 등을 메타데이터로 저장하기 때문에, 운영 및 모니터링 편의성이 크게 향상됩니다.

</aside>

## 5. 한계 및 개선 방안

<aside>

* **한계 (MSA 전환 시 확장성 문제):**
    * 현재 배치가 하나의 DB에 직접 접근하는 모놀리식 구조입니다.
    * 서비스가 MSA로 분리되면, 배치가 각 도메인의 데이터베이스를 직접 조회할 수 없게 되는 문제가 발생합니다.

* **개선 방안 (Event Sourcing + CDC 활용):**
    * 각 도메인 서비스가 데이터 변경 시 이벤트를 발행하여 Kafka 등의 메시지 스트림으로 흘려보냅니다.
    * 배치 애플리케이션은 이 이벤트들을 구독(Subscribe)하여 Batch 전용 DB(또는 데이터 레이크)에 저장합니다.
    * Spring Batch는 외부 DB가 아닌, 이 데이터를 기반으로 정산 배치를 수행합니다.

</aside>

</details>

<details>
<summary><b>RabbitMQ</b></summary>

## 1. 도입 배경

<aside>

매치가 시작하면 매치 상태값이 “ON_GOING”으로 업데이트 되어야 한다.

</aside>

## 2. 기술적 요구사항

<aside>

* **정확한 실행 시점 보장**: 매치 시작 시간에 맞춰 상태값이 SCHEDULED → ON_GOING으로 변경되어야 한다.

</aside>

## 3. 의사결정 과정

<aside>

1.  **Redis Keyspace Notification**
    * **장점:** Redis의 TTL 만료 이벤트를 활용하여 구현이 비교적 단순하고, 별도 메시지 브로커를 두지 않아도 된다.
    * **단점:** Keyspace notification은 알림 중심 기능으로, 메시지 유실에 대한 대비가 힘듬. Redis는 대량의 이벤트 처리, 재시도, 운영 기능 구현이 따로 필요하다.

2.  **RabbitMQ DelayQueue**
    * **장점:**
        * 메시지는 큐에 적재된 상태로 브로커에서 관리되기 때문에, 애플리케이션이 재시작되어도 메시지가 사라지지 않는다.
        * DLX(Dead Letter Exchange)를 통한 실패 메시지 분리 및 재처리가 가능하여 운영 안정성이 높다.
        * Producer(매치 생성/수정)와 Consumer(매치 시작 처리)를 명확히 분리할 수 있어, MSA나 수평 확장에 유리하다.
    * **단점:** 배포 시 별도의 운영 서버가 필요하다.

3.  **Kafka**
    * **장점:** 파티션 기반 수평 확장을 통해 수백만 건 이상의 이벤트 스트림도 안정적으로 처리 가능.
    * **단점:**
        * **DelayQueue 구현:** RabbitMQ처럼 TTL 설정만으로 지연처리가 되는 구조가 아니어서 스케줄링 로직을 직접 개발해야 하고 구현 복잡도가 높다.
        * **운영 부담:** 브로커, 파티션, 리플리카, 모니터링 등 운영 요소가 많아 인프라 관리 비용과 복잡도가 크게 증가한다.
        * **과한 스펙:** 현재 기능은 정확성 중심의 단일성 이벤트 처리이기 때문에 Kafka의 고처리량 구조를 충분히 활용할 만큼의 볼륨이 아니다.

</aside>

## 4. 최종 선택 이유

<aside>

1.  **ms 단위 Delay 기반의 높은 시간 정확성**
    * RabbitMQ DelayQueue는 각 메시지에 TTL(만료 시간)을 부여해, ms 단위까지 지연 시간을 설정할 수 있다.

2.  **비동기 처리 + 영속성 보장으로 인한 운영 안정성**
    * 메시지는 브로커에 적재되며, 브로커가 TTL, 라우팅, 재시도, DLX를 책임진다.
    * 운영 장애가 발생해도 큐에 남아 있는 메시지는 그대로 유지된다.
    * Consumer 인스턴스를 여러 개 띄워 **부하 분산** 가능하다.
    * 처리 실패 시 DLX로 보내 두고, 별도 배치/관리 로직으로 재처리할 수 있다.

</aside>

## 5. 한계 및 개선 방안

<aside>

* **배포 운영성 문제:**
    * AWS에서 RabbitMQ의 DelayQueue를 지원하지 않기 때문에 RabbitMQ 브로커를 EC2 또는 컨테이너로 직접 구축해 운영해야 한다.
 
* **처리량 한계:**
    * 수 백만 건의 지연 메시지가 누적되면 메모리/스토리지 부담이 커지고 처리 속도도 저하된다.

* **Kafka 도입:**
    * Kafka 기반 DelayQueue를 도입하면 RabbitMQ 대비 훨씬 높은 처리량을 확보할 수 있다.
    * AWS MSK를 사용하여 배포 운영성 문제도 개선 가능하다.
    * AWS MSK (Managed Streaming for Kafka)를 사용하면 배포 운영성 문제도 함께 개선 가능합니다.

</aside>

</details>

<details>
<summary><b>Amazon EventBridge + AWS Lambda</b></summary>

## 1. 도입 배경

<aside>

* 배치 서버는 상시 구동이 불필요
* 기존 배치 프로그램은 Spring Batch와 Spring Boot로 작성

</aside>

## 2. 기술적 요구사항 (목표)

<aside>

* 배치 서버를 필요할 때만 실행

</aside>

## 3. 의사결정 과정

<aside>

여러 방안을 검토

| 구분 | 장점 | 단점 |
| --- | --- | --- |
| **EventBridge + Lambda** | - 비용 부담 적음<br>- 추가 구현 불필요 | - EC2 부팅 시간<br>- EC2 인스턴스 관리 필요 |
| **AWS Batch** | - 큐 관리, 재시도 로직 내장<br>- 로깅 및 모니터링 지원 | - 설정 복잡도 높음 |
| **ECS Fargate** | - 인프라 관리 부담 감소<br>- 세밀한 리소스 제어 | - 컨테이너화 및 CI/CD 추가 구현 필요<br>- EC2에 비해 높은 가격 |

</aside>

## 4. 최종 선택 이유

<aside>

**EventBridge (스케줄링) + Lambda** 방식 선택

1.  **단순성:** 현재 배치는 단일 잡(Job)으로 구성되어 있어, AWS Batch 같은 복잡한 잡 오케스트레이션(의존성 관리, 큐) 기능이 불필요
2.  **낮은 마이그레이션 비용:** 기존 Spring Batch 코드를 그대로 활용하는 것이 목표였습니다. ECS Fargate나 AWS Batch를 위해 컨테이너화(Dockerfile 작성) 및 마이그레이션에 드는 학습 비용과 CI/CD 파이프라인 수정 비용을 회피

</aside>

## 5. 한계 및 개선 방안

<aside>

* 확장성 필요 시 별도 CI/CD 파이프라인 + 컨테이너화
* 잡 간 의존성 도입 또는 유동적 잡 부하 → ECS Fargate나 AWS Batch로 전환 고려

</aside>

</details>


<details>
<summary><b>Groq</b></summary>

## 1. 도입 배경

<aside>

경기 승률 예측을 위한 AI 모델 도입

</aside>

## 2. 기술적 요구사항

<aside>

OpenAI를 도입해 사용자가 입력한 질문을 통해 적절한 응답을 받을 수 있도록 함

</aside>

## 3. 의사결정 과정

<aside>

Spring AI 적용을 시작하여 OpenAI를 사용해 ChatGPT, Gemini를 연결해보려 했으나, 다음과 같은 오류가 발생했다.

```bash
org.springframework.ai.retry.NonTransientAiException: 429 - {
  "error": {
    "message": "You exceeded your current quota, please check your plan and billing details.",
    "type": "insufficient_quota",
    "code": "insufficient_quota"
  }
}
```

429 오류로 OpenAI API 요금제(Quota) 문제가 발생했다. 이 문제를 해결하기 위해 조사하던 중 `Groq AI`를 발견했다.

</aside>

## 4. 최종 선택 이유

<aside>

`Groq AI`를 선택한 이유

**장점**

- **무료:** 별도의 비용 없이 사용 가능
- **빠른 응답:** 테스트 기준으로 동일한 프롬프트를 사용했을 때, GPT-4는 약 4~6초, Claude는 약 2~4초, Groq(Mixtral)는 1초 이내로 ⚡ 번개처럼 빠른 응답 속도를 제공
- **일일 토큰 제한 없음:** 사용량 제약이 없어 안정적인 서비스 운영 가능
- **예상 과금 금액 자동 계산:** 투명한 비용 예측
- **NPU 기반 고성능:** GPU가 아닌 AI LLM에 특화되어 만들어진 NPU 기반으로 고성능 제공

**단점**

- GPT에 비해 성능이 아쉬움
- 간단하고 빠른 작업에만 특화
- Spring AI가 Groq의 최신 기능을 아직 미지원할 수 있음

ChatGPT처럼 다양한 분야에 대한 답변에서는 취약할 수 있으나, 단순 연산 및 한정된 분야에 도입하기에 적합하다고 판단하여 우선적으로 도입하기로 결정했다.

</aside>

</details>

<details>
<summary><b>Grafana & Prometheus</b></summary>

## 1. 도입 배경

<aside>

성능 테스트 및 장애 모니터링을 위한 기술 스택 도입이 필요해짐

</aside>

## 2. 기술적 요구사항

<aside>

메트릭을 수집하고 이를 시각화함으로써 프로젝트 전체를 관리하게 함

</aside>

## 3. 의사결정 과정

<aside>

`Prometheus`와 `Grafana` 두 가지 기술을 검토했다.

**Prometheus**

- Pull 방식의 메트릭 수집
- 안정적인 보안 체계
- 오픈소스이기에 비용 효율적
- 메트릭 데이터와 호환성이 좋음
- 안정성과 신뢰성
- 다양한 플러그인 제공

**Grafana**

- 모든 데이터를 Query 기반으로 변환하여 표시
- Multi-source observability 지원 (다양한 데이터 소스 통합)
- 다양한 플러그인 제공
- 사용자 친화적 대시보드
- 실시간 모니터링

**Pull 방식의 보안 우위성**

```
Pull 방식은 모니터링 시스템이 애플리케이션에 메트릭을 요청하고, 애플리케이션은 메트릭을 제공하는 방식이기 때문에, 애플리케이션 측에서는 데이터의 수집을 허용할 IP 주소나 데이터 수집기의 인증서를 검증하는 등의 추가적인 보안 설정이 가능하다.

반면, Push 방식은 애플리케이션이 모니터링 시스템에 메트릭을 전송하는 방식이기 때문에, 모니터링 시스템 측에서 데이터를 전송하는 서버의 IP 주소나 인증서 등을 검증할 수 없다. 이로 인해 보안상 취약점이 존재할 수 있다.

따라서 보안상의 이유로 Pull 방식을 사용하는 것이 권장된다.
```

**Prometheus + Grafana 조합의 장점**

- **완벽한 모니터링 스택:** Prometheus의 데이터 수집 및 저장과 Grafana의 시각화 및 알림이 서로 완벽하게 호환됨
- **클라우드 네이티브:** Kubernetes 환경에 최적화되고, 컨테이너 기반 배포 지원, Auto-discovery 기능 제공
- **확장성:** 대규모 메트릭 처리 가능, 수평적 확장 지원, 풍부한 플러그인 생태계
- **역할 분리:** 데이터 수집과 시각화로 역할 분리가 명확하여 확장성과 유연성에 유리

</aside>

## 4. 최종 선택 이유

<aside>

위의 장점들을 종합하여 메트릭 수집 스택으로 Pull 방식의 `Prometheus`를 도입하기로 결정했으며, 완벽한 호환성을 고려하여 `Grafana`를 시각화 스택으로 도입하기로 최종 결정했다.

</aside>

</details>


<details>
<summary><b>LangGraph4j</b></summary>

## 1. 도입 배경

<aside>

AI Tool의 증가와 챗봇의 고도화를 위해 보다 복잡한 플로우를 관리하는 스택이 요구됨

</aside>

## 2. 기술적 요구사항

<aside>

여러 Tool을 상황에 맞게 사용하며, 개발자가 명령하는 것이 아닌 AI의 자발적 사고 후 처리를 요망

</aside>

## 3. 의사결정 과정

<aside>

**LangGraph란?**

`LangGraph`는 자연어 처리와 AI 응용 프로그램 개발을 위한 강력한 프레임워크로, 복잡한 언어 모델과의 상호작용을 효율적이고 구조화된 방식으로 구현할 수 있도록 돕습니다. 다양한 데이터 소스와 언어 모델을 통합하여 지능형 응답 생성, 정보 검색, 텍스트 분석 등의 고도화된 시스템을 구축할 수 있습니다. `LangGraph4j`는 파이썬 라이브러리인 LangGraph를 자바에서 지원하도록 만들어져 파이썬 서버를 별도로 띄울 필요 없이 간편히 구현이 가능합니다.

**LangGraph의 장점**

- **복잡한 로직 구현:** 다단계 의사결정 프로세스나 복잡한 워크플로우를 쉽게 구현 가능
- **세밀한 제어:** 애플리케이션의 각 단계를 정밀하게 제어하여 고도로 커스터마이즈된 동작 구현 가능
- **확장성:** 서브그래프를 통해 대규모 시스템을 모듈화하여 관리 가능
- **상태 지속성:** 체크포인팅을 통해 장기 실행 태스크와 오류 복구 용이
- **다중 에이전트 시스템:** 여러 AI 에이전트의 상호작용을 효과적으로 모델링 가능

**LangChain과의 차이점**

| 항목 | LangChain 단독 사용 | LangGraph 도입 후 |
|------|------------------|------------------|
| 실행 흐름 구조 | 직렬적 체인 중심 (순차 실행) | 그래프 구조 (조건 분기, 루프, 병렬 처리 가능) |
| 상태 관리 | 제한적 (컨텍스트 관리에 별도 코드 필요) | 내장된 State 객체로 세밀한 관리 가능 |
| 복잡한 시나리오 구현 | 다단계 체인 중첩으로 복잡도 증가 | 노드/엣지 설계로 직관적이고 유지보수 용이 |
| 제어 흐름 | if/else, 반복 로직 직접 구현 필요 | LLM 중심 조건 분기와 반복을 프레임워크 차원에서 지원 |
| 기존 자산 활용 | LangChain 컴포넌트 단독 사용 | LangChain 컴포넌트를 그래프 노드로 재활용 가능 |
| 확장성 | 체인 기반 확장에는 코드 리팩토링 부담 | 노드 단위 교체/추가만으로 확장 가능 |
| 디버깅/모니터링 | 실행 경로 추적 어려움, 로그 위주 | 그래프 단위 실행 추적, 노드별 입출력 기록 |
| 적합한 활용 사례 | 단순 파이프라인, 빠른 프로토타입 | 대규모 LLM 워크플로우, 멀티턴 대화, RAG, 에이전트 오케스트레이션 |

</aside>

## 4. 최종 선택 이유

<aside>
<img width="591" height="432" alt="image" src="https://github.com/user-attachments/assets/00f9657c-ed25-4007-a8af-99246a9fc679" />


이번 프로젝트의 챗봇에는 승률 예측 외에도 E-Sports 관련 인기 검색어, 최신 뉴스, 경기 일정 등 다양한 역할의 Tool들이 존재하며, AI 챗봇에 대한 안정적이고 유연한 대처를 필요로 했습니다. 복잡한 플로우 속에서도 AI가 스스로의 판단으로 결정을 내려 행동하고, LLM의 답변을 추가 검증하며, 다중 에이전트를 통한 여러 에이전트를 관리할 수 있다는 장점이 이러한 요구사항에 부합한다고 판단했습니다. 따라서 `LangGraph4j`를 도입하여 고도화된 다중 에이전트 시스템을 구축하기로 결정했습니다.

</aside>

</details>

<details>
<summary><b>Amazon Prometheus</b></summary>

## 1. 도입 배경

<aside>

종속적이지 않은 배포 환경의 모니터링 기술

</aside>

## 2. 기술적 요구사항

<aside>

```
전체 아키텍처

[App / Node Metrics]  
       │
       ▼
[Prometheus Operator on EKS]  →  Remote Write  →  [AMP Workspace]  
                                           │
                                           ▼
                                   [Grafana Cloud] (Query)
```

EKS 내부에서 동작 중인 애플리케이션이 `/actuator/prometheus` 엔드포인트를 통해 메트릭 노출하고, Prometheus가 수집한 메트릭을 AMP로 전달하며, Grafana에서 AMP를 데이터 소스로 연결해 시각화함

</aside>

## 3. 의사결정 과정

<aside>

**문제 정의**

기존 프로그램의 Docker Container를 통한 Prometheus + Grafana 기술 스택은 Docker에 종속적이어서 다른 환경에서의 동작을 보장할 수 없었으며, 배포되지 않은 상태로 로컬 환경에서만 접근 가능했다. 이를 배포 환경에서도 모니터링할 수 있도록 개선해야 했다.

**기술 선택지**

- **Prometheus:** Pull 방식의 메트릭 수집, 안정적인 보안 체계, 오픈소스 기반 비용 효율성, 메트릭 데이터 호환성 우수
- **Grafana:** Query 기반 데이터 변환, Multi-source observability 지원, 사용자 친화적 대시보드, 실시간 모니터링
- **Prometheus + Grafana:** 완벽한 호환성, Kubernetes 환경 최적화, Auto-discovery 기능, 대규모 애플리케이션 관리 가능

**EKS vs ECS**

EKS (Elastic Kubernetes Service)는 오픈 소스 Kubernetes 기반 애플리케이션을 AWS에서 운영하는 서비스로, 간편한 클러스터 생성, Kubernetes의 Container Orchestration 기능 활용(롤링배포, Auto Scaling, 자동 복구), AWS 종속성 제거, 대규모 애플리케이션 관리 가능이 장점이며, 높은 유지 비용과 관리 복잡성이 단점이다.

ECS (Elastic Container Service)는 완전관리형 컨테이너 오케스트레이션 서비스로, AWS 최적화를 통한 단순함, 손쉬운 배포 및 운영, AWS 서비스 연동 편리함, 클러스터 관리 추가비용 없음이 장점이며, EC2 기반의 경우 직접 관리 필요, AWS 플랫폼 종속성으로 인한 유연성 및 이식성 저하가 단점이다.

Kubernetes의 특성을 활용하면서 대규모 복잡한 아키텍처를 필요로 하고, Kubernetes 운영 전문 인력이 있으며, 여러 클라우드 환경에서의 이식성이 필요한 상황에서는 EKS가 적합하다.

추가) IRSA ROLE 설정을 채택하게 된 이유

<img width="601" height="650" alt="image" src="https://github.com/user-attachments/assets/7039ecc7-1dc0-45cd-9099-18afe0c21fe7" />


**AMP (Amazon Managed Prometheus)**

`AMP`는 AWS에서 관리하는 Prometheus 호환 서비스로, 다음과 같은 특징을 가진다.

- **장점:** 다수의 AWS 가용 영역에 걸쳐 고가용성 제공, 수집된 메트릭을 Amazon S3에 저장해 최대 150일 보관, 강력한 Auto Scaling, 완전 관리형으로 운영 부담 절감, Grafana에서 DataSource로 직접 연결, PromQL 100% 동일로 Prometheus와 완전 호환
- **단점:** 수집 메트릭 양에 따른 비용 증가

**Prometheus Operator vs 일반 Prometheus Helm Chart**

Prometheus Operator (kube-prometheus-stack)는 `Prometheus`, `Alertmanager`, `ServiceMonitor`, `PodMonitor` 같은 CRD로 Kubernetes 리소스처럼 관리 가능하며, 새로운 ServiceMonitor가 생기면 자동으로 Prometheus가 감지해 스크래핑한다. 여러 Prometheus 인스턴스를 운영 가능하고 리소스별 별도 관리가 쉬우며, Alertmanager, Grafana 등 종합적인 모니터링 스택을 한 번에 설치할 수 있다. 다만 CRD 개념 이해가 필요하고 구조가 복잡하며, ServiceMonitor 같은 리소스 개념을 익혀야 한다.

일반 Prometheus Helm Chart는 Deployment + ConfigMap 방식으로 직관적이고 제어가 쉬우며 간단한 테스트나 PoC에 적합하다. 하지만 여러 Prometheus 인스턴스 운영이 어렵고, 자동 스크래핑 지원 부족, Grafana/Alertmanager를 별도로 설치해야 한다.

**상황에 따른 선택**

AMP, Grafana, Alertmanager까지 운영 환경에서 사용할 계획이라면 `kube-prometheus-stack (Prometheus Operator)`을 선택하고, 테스트 환경에서 Prometheus만 필요하면 `prometheus-community/prometheus`를 선택한다.

</aside>

## 4. 최종 선택 이유

<aside>

Kubernetes의 풍부한 기능과 유연한 확장성을 활용하기 위해 `EKS`와 `Prometheus Operator 기반 kube-prometheus-stack`을 도입하기로 결정했습니다.

현 상황의 소규모 프로젝트에 적용하기에 오버스택일 수 있으나, Prometheus + Grafana의 클라우드 네이티브 특성상 Kubernetes 환경에 최적화되어 있으며, 추후의 확장 가능성과 장기적인 기술 관리를 종합적으로 고려했습니다.

EKS는 강력한 스케줄링 및 롤링 배포 기능 외에도 CRD를 활용한 맞춤형 오케스트레이션이 용이했으며, AMP, Grafana, Alertmanager까지 운영 환경에서 사용할 예정이었기에 여러 서버 관리에 적합했습니다.

장기적으로는 특정 클라우드에 대한 종속성을 줄이고 멀티 클라우드 전략 가능성을 열어두는 측면에서도 EKS 도입이 최선의 선택이라고 판단했습니다.

</aside>

</details>

<details>
<summary><b>REST Docs</b></summary>

## 1. 도입 배경

<aside>



</aside>

## 2. 기술적 요구사항

<aside>



</aside>

## 3. 의사결정 과정

<aside>



</aside>

## 4. 최종 선택 이유

<aside>


</aside>

</details>

<details>
<summary><b>Spring Scheduler</b></summary>

## 1. 도입 배경

<aside>



</aside>

## 2. 기술적 요구사항

<aside>



</aside>

## 3. 의사결정 과정

<aside>



</aside>

## 4. 최종 선택 이유

<aside>


</aside>

</details>

---

## 📈 성능 개선

<details>
<summary><b>[매치] 매치 상세 조회 성능 개선</b></summary>

## 1. 도입 배경

<aside>

`GET /api/v1/matches/{id}` (매치 상세 조회) API에 조회 로직과 조회수 증가 로직이 하나의 트랜잭션으로 묶여있어 캐시 적용이 불가능했고, 모든 요청이 DB에 `SELECT`와 `UPDATE` 쿼리를 실행했다.

k6 (VUser 500명) 부하 테스트 시 발생하는 DB 커넥션 풀 고갈(HTTP 500) 문제를 해결하고, 캐싱을 적용하여 API 응답 속도를 10ms 내외로 단축하는 것을 목표로 했다.

</aside>

## 2. 성능 개선 과정

<aside>

| 버전 | 적용 내용 | DB 쿼리 (요청당) | p(95) 응답 시간 | TPS (k6) | 결과 (병목 지점) |
|------|---------|-----------------|-----------------|----------|-----------------|
| V1 | 기존 코드 | 1 UPDATE, 1 SELECT | 30,017ms (Timeout) | 109/s | 실패 (DB 병목) |
| V2 | 로직 분리 (CQRS) | 1 UPDATE, 1 SELECT | 30,772ms (Timeout) | 12/s | 실패 (DB 병목) |
| V3 | 캐싱 적용 (SELECT 제거) | 1 UPDATE, 0 SELECT | 30,824ms (Timeout) | 9/s | 실패 (UPDATE 병목) |
| V4 | Redis INCR 적용 (UPDATE 제거) | 0 UPDATE, 0 SELECT | 15ms | 2,450/s | **성공 (병목 해결)** |

</aside>

## 3. 적용 결과

<aside>

**1차 & 2차 테스트: DB 병목 확인**

V1 (원본)은 `getMatch` (`SELECT` + `UPDATE`)로 구현되었고, V2 (로직 분리)는 `incrementViewCount` (`UPDATE`) + `getMatch` (`SELECT`)로 분리되었다.

k6 VUser 500명 부하 시, 두 버전 모두 10개의 DB 커넥션 풀이 즉시 고갈되었다. 대기열(Queue)에서 30초 이상 기다린 요청들이 `i/o timeout`으로 실패하며, 테스트 에러율 100%를 기록했다. (p(95) 응답 시간 30초 이상)

**결론:** DB 커넥션을 점유하는 것이 1차 병목이다.

---

**3차 테스트: 캐싱 적용 후, 2차 병목 발견**

V3 (캐싱 적용)은 `incrementViewCount` (DB UPDATE) + `getMatch` (@Cacheable)로 구현되었다. `getMatch`의 `SELECT` 쿼리는 Redis로 대체되어 캐싱에는 성공했다.

하지만 500명의 유저가 `incrementViewCount` (DB UPDATE)를 동시에 호출하면서, 5개의 Match Row에 Row Lock 경합이 발생했다. 이로 인해 DB 커넥션 풀이 다시 고갈되었고, 여전히 `i/o timeout` 오류가 발생했다.

**결론:** SELECT 병목 해결 후, UPDATE로 인한 Row Lock이 새로운 병목이 되었다.

---

**4차 테스트: 최종 고도화 (Redis INCR)**

V4 (최종)는 `incrementViewCount` (Redis INCR) + `getMatch` (@Cacheable)로 구현되었다. 조회수 증가 로직을 DB UPDATE에서 Redis의 INCR(원자적 증가)로 변경하여 DB 접근이 0회가 되었다.

500 VUser 부하에서 `i/o timeout`이 완전히 사라졌다. p(95) 응답 시간은 15ms로 측정되었고, TPS는 2,450/s로 V1 대비 **22배 이상 향상**되었다.

**결론:** 캐싱과 Redis INCR을 조합하여 API 성능을 최적화했다.

</aside>

</details>

<details>
<summary><b>동시성 제어 (Redisson 분산 락)</b></summary>

## 1. 도입 배경

<aside>

베팅 및 포인트 지급은 유저의 포인트를 직접 변동시키는 핵심 트랜잭션이다. 초기에는 비관적 락을 사용했는데, 이 방식은 단일 서버에서는 데이터 정합성을 보장하지만, 서버가 2대 이상으로 분산되면 DB 커넥션 풀을 점유하여 i/o timeout 및 시스템 전체 장애를 유발하게 된다.

서버가 10대 이상으로 확장되더라도 데이터 정합성(예: 포인트가 0 미만으로 내려가지 않음)을 보장하며, DB 커넥션 풀을 고갈시키지 않고, 락 획득에 실패한 요청은 정상적인 거부로 빠르게 응답하여 시스템 다운을 방지하는 것을 목표로 했다.

</aside>

## 2. 성능 개선

<aside>

**락 방식 비교**

| 구분 | 개념 | 장점 | 단점 |
|------|------|------|------|
| **비관적 락** (Pessimistic) | `SELECT FOR UPDATE`로 DB Row에 락을 검 | 데이터 정합성을 확실히 보장 | DB 커넥션 풀을 점유하여, 부하 시 시스템 전체가 멈춤 |
| **분산 락** (Distributed) | 중앙 저장소(Redis)를 통해 락을 제어 | 서버가 몇 대든 정합성 보장 | Redis 의존성, 구현 복잡성(AOP) |

**아키텍처 설계**

- **락 담당 계층** (`BetService` / `UserService`)
  - `RedissonClient`를 주입받아 `tryLock()` / `unlock()`을 실행
  - AOP의 셀프 호출 문제를 피하기 위해 `@Transactional` 로직을 별도의 서비스로 분리

- **DB 담당 계층** (`BetTransactionService` / `UserPointTransactionService`)
  - 실제 DB 로직(`@Transactional`)만을 담당
  - 락이 이미 획득된 상태에서만 호출되므로, `findById`를 사용하여 DB 락을 잡지 않음

</aside>

## 3. 적용 결과

<aside>

**데이터 정합성 검증**

`BetServiceConcurrencyTest` 통합 테스트 결과, 20개의 동시 요청이 발생해도 유저 포인트가 0 미만으로 내려가지 않고 정확히 10회만 차감됨을 검증했다. 이는 분산 환경에서 완전한 데이터 정합성을 보장함을 의미한다.

**안정성 개선**

비관적 락에서 발생하던 HTTP 500 에러(100%)가 0%로 개선되었다. 분산 락 도입으로 DB 커넥션 풀 고갈 문제가 완전히 해결되었다.

**사용자 경험 개선**

락 경합 시(`tryLock` 실패), 무한정 대기하는 대신 `BET_LOCK_FAILED` (HTTP 409 Conflict) 예외를 즉시 반환하여 "잠시 후 다시 시도하세요"라는 명확한 피드백을 제공한다. 이를 통해 사용자는 시스템 장애가 아닌 일시적 경합 상황으로 인식하게 되어 재시도 의도를 자연스럽게 유도할 수 있다.

</aside>

</details>

<details>
<summary><b>Elasticsearch 도입을 통한 검색 성능 개선</b></summary>

### 1. 도입 배경

#### 1.1 현재 검색 구현 기능

```java
//MatchRepositoryImpl.java

builder.and(match.matchName.containsIgnoreCase(condition.keyword())
        .or(match.teamA.containsIgnoreCase(condition.keyword()))
        .or(match.teamB.containsIgnoreCase(condition.keyword())));
```

SQL 변환

```sql
WHERE (LOWER(match_name) LIKE LOWER('%keyword%')
    OR LOWER(team_a) LIKE LOWER('%keyword%')
    OR LOWER(team_b) LIKE LOWER('%keyword%'))
```

#### 1.2 MySQL LIKE의 한계

- **Full Table Scan**
  - `%keyword%`는 인덱스를 사용할 수 없다.
  - 모든 행을 스캔해야 하므로 O(n) 시간 복잡도
  - 데이터가 증가할수록 선형적으로 성능 저하

- **OR 조건으로 인한 성능 악화**
  - 3개 컬럼에 대한 OR 검색
  - 각 컬럼마다 Full Table Scan 수행
  - 실제로 3배를 스캔하는 비용 발생

- **LOWER() 함수로 인한 성능 악화**
  - 모든 행에 대한 대소문자 변환 수행

- **동시성 문제**
  - 여러 사용자가 검색 요청 시 Lock Contention 발생

---

### 2. 테스트 전략

#### 2.1 목표

데이터 증가 및 동시 사용자 증가 시 MySQL LIKE 검색의 성능 저하를 정량적으로 입증

#### 2.2 입증할 포인트

- 데이터 크기에 따른 성능 저하
- 동시 사용자 증가에 따른 성능 악화

---

### 3. 테스트 시나리오

#### 3.1 데이터 볼륨 영향도 테스트

- **TEST 1:** 1만건 데이터 검색
  - VUs: 10명
  - Duration: 4분

- **TEST 2:** 5만건 데이터 검색
  - VUs: 10명
  - Duration: 4분

- **TEST 3:** 10만건 데이터 검색
  - VUs: 10명
  - Duration: 4분

---

### 4. 성능 테스트 결과

**MySQL 상세 성능 데이터**

<img width="727" height="204" alt="image" src="https://github.com/user-attachments/assets/13df4dfb-f545-4baa-a12b-13e92daa7699" />


**시간 복잡도 분석**

MySQL의 시간 복잡도: O(n) 이상의 패턴

데이터가 누적됨에 따라 응답 시간이 선형 이상으로 증가하는 패턴. LIKE 검색이나 Full Text Search의 경우 데이터가 많아지면 스캔 범위가 증가한다.

---

### 5. MySQL VS Elasticsearch

#### 평균 응답 시간 비교

<img width="581" height="447" alt="image" src="https://github.com/user-attachments/assets/c1f278f1-8342-45c8-b19c-54a5304671ba" />


| 검색 엔진 / 데이터 개수 | 1만건 | 5만건 | 10만건 | 평균 |
| --- | --- | --- | --- | --- |
| MySQL LIKE | 81.67 ms | 179.49 ms | 305.39 ms | 188.85 ms |
| Elasticsearch | 61.07 ms | 30.33 ms | 35.86 ms | 42.36 ms |

#### 응답 시간 백분위수 비교

<img width="445" height="447" alt="image" src="https://github.com/user-attachments/assets/ab6c09d3-b72c-4950-80b9-0ebbe4653d0a" />


| 검색 엔진 / 백분위 | min | p50 | p90 | p95 | max |
| --- | --- | --- | --- | --- | --- |
| MySQL LIKE | 75.05 ms | 111.35 ms | 236.61 ms | 441.10 ms | 3,442.20 ms |
| Elasticsearch | 10.34 ms | 25.69 ms | 73.27 ms | 126.69 ms | 1,020.36 ms |

#### Elasticsearch의 우수성

<img width="574" height="423" alt="image" src="https://github.com/user-attachments/assets/ac78bc97-99ce-4430-96e7-15beb5ba77af" />


- 거의 일정한 응답 시간 유지
- O(log n)에 가까운 성능: 역색인 구조로 데이터 증가에 거의 영향 받지 않음
- 확장성: 데이터가 더 많아져도 안정적인 성능 보장

**테스트 결과 정리**

MySQL 평균 응답 시간은 188.85 ms, Elasticsearch 평균 응답 시간은 42.36 ms로 **77.6% 성능 개선율**을 보임.

---

### 6. 테스트 시나리오 - 동시성 테스트

#### 6.1 여러 사용자가 동시에 API 호출

- VUs: 100명
- Duration: 4분 30초

**임계값 설정**

```javascript
thresholds: {
  'http_req_duration': [
    'p(50)<500',    // 50% 요청이 0.5초 미만 (median, 일반적 사용자 경험)
    'p(95)<2000',   // 95% 요청이 2초 미만 (허용 가능한 최대치)
    'p(99)<5000',   // 99% 요청이 5초 미만 (극단적 상황)
    'max<10000'     // 최악의 경우도 10초 이내
  ],
  'http_req_failed': ['rate<0.01'],  // 실패율 1% 미만 (100명 중 1명)
  'errors': ['rate<0.01'],            // 에러율 1% 미만
  'timeouts': ['rate<0.005'],         // 타임아웃 0.5% 미만 (200명 중 1명)
  'search_response_time': ['p(95)<2000'],  // 커스텀 메트릭 임계값
},
```

---

### 7. MySQL vs Elasticsearch (동시성 환경)

#### 평균 응답 시간 비교

<img width="678" height="454" alt="image" src="https://github.com/user-attachments/assets/6d757c0f-6544-4335-b911-f628e924ebdf" />


| 검색 엔진 | 응답 시간 |
| --- | --- |
| MySQL | 4,169.92 ms |
| Elasticsearch | 70.91 ms |

#### 응답 시간 백분위수 비교

<img width="678" height="454" alt="image" src="https://github.com/user-attachments/assets/7d025b61-f573-42f2-a203-bfd4ef851a23" />


| 검색 엔진 / 백분위 | min | p50 | p90 | p95 | max |
| --- | --- | --- | --- | --- | --- |
| MySQL | 132.06 ms | 3,933.66 ms | 5,829.05 ms | 7,523.29 ms | 17,221.97 ms |
| Elasticsearch | 5.18 ms | 21.12 ms | 131.17 ms | 245.33 ms | 2,999.59 ms |

#### 처리량 및 성능 지표

<img width="678" height="454" alt="image" src="https://github.com/user-attachments/assets/5824c021-4a1e-4f29-9314-c543c13852cd" />


| 검색 엔진 | 처리량 | 총 요청수 |
| --- | --- | --- |
| MySQL | 15.4 req/s | 41.76 |
| Elasticsearch | 62.3 req/s | 170.07 |

#### 상세 성능 지표

<img width="718" height="582" alt="image" src="https://github.com/user-attachments/assets/bc215281-6c59-45b4-9f17-a9a0f2714a0d" />


#### 안전성 비교

<img width="706" height="482" alt="image" src="https://github.com/user-attachments/assets/5d05b691-aac0-4ca5-9e30-f01697e9b667" />


---

### 8. 최종 분석

#### MySQL의 주요 문제점

- **응답 시간 허용 범위 실패율: 19.4%** - 812건의 요청이 허용 범위를 초과
- **느린 쿼리 발생: 3,824건** - 전체 요청의 상당 부분이 느린 쿼리로 분류됨
- **평균 응답 시간: 4,170 ms (4.17초)** - 실용적 서비스 제공 불가능
- **최대 응답 시간: 17,222 ms (17.2초)** - 극단적인 지연 발생

#### Elasticsearch의 주요 장점

- **응답 시간 허용 범위: 100% 통과** - 모든 요청이 허용 범위 내에서 처리
- **느린 쿼리: 단 5건** - 거의 모든 쿼리가 빠르게 처리됨
- **평균 응답 시간: 71 ms** - MySQL 대비 **58배 빠름**
- **처리량: 62.3 req/s** - MySQL(15.4 req/s)의 **4배 처리 능력**

</details>

<details>
<summary><b>[매치] 매치 생성 성능 개선</b></summary>

## 1. 도입 배경

<aside>

매치 스케줄 저장 시 매치 단위로 개별 INSERT 쿼리가 반복 실행되는 구조로 인해 DB Connection 생성·반환 오버헤드가 누적되고, DB 통신 비용까지 증가하여 비효율적인 I/O 병목이 발생했다.

동일 시점에 수집되는 대량의 매치 데이터를 보다 효율적으로 저장하여 전체 배치 처리 시간을 단축하고, 서비스 전반의 DB 부하를 최소화하는 것을 목표로 했다.

</aside>

## 2. 성능 개선

<aside>

**기존 방식: JPA saveAll() 기반 엔티티 저장**

JPA의 `saveAll()` 메서드는 내부적으로 개별 INSERT 쿼리가 반복 실행되며, JPA의 영속성 컨텍스트 관리 비용까지 발생해 대량 데이터 처리에는 비효율적이다.

```
개별 INSERT × N건 → DB Connection 생성·반환 반복 → 누적된 오버헤드
```

**개선 방식: JdbcTemplate 기반 Batch Insert**

JdbcTemplate의 배치 처리 방식은 Chunk 단위 500건씩 데이터를 바인딩해 네트워크 왕복 횟수를 500 → 1로 줄이고, JDBC 드라이버의 batch 최적화를 활용해 대규모 INSERT를 한 번에 수행하기 때문에 saveAll 대비 훨씬 높은 처리량을 확보한다.

```
500건 배치 처리 → 네트워크 왕복 1회 → DB 배치 최적화 활용 → 처리량 극대화
```

</aside>

## 3. 적용 결과

<aside>

**처리 시간 개선**

- 기존 (saveAll): 2,500 ms
- 개선 (Batch Insert): 1,500 ms
- **성능 개선율: 약 60% 단축**

JdbcTemplate 기반 Batch Insert 도입으로 매치 대량 저장 시 DB 통신 비용과 Connection 오버헤드를 크게 줄이며, 안정적인 배치 처리 성능을 확보했다.

</aside>

## 4. 회고

<aside>

JdbcTemplate Batch Insert는 분명 알고 있는 기술이었지만, 더 최신이고 좋은 기술을 생각하느라 막상 생각이 잘 떠오르지 않았다. JPA와 같이 쉽고 간편하면서 유용하기까지 한 기술들이 많지만, 상황에 따라 기본으로 돌아가는 다양한 시각과 넓은 안목을 길러야 한다는 것을 배웠다.

</aside>

</details>

<details>
<summary><b>[팀] 팀 상세 정보 조회 성능 개선</b></summary>

## 1. 도입 배경

<aside>

팀 상세 정보 조회 API (`GET /api/v1/teams/{id}`)에서 매 요청마다 데이터베이스에 접근하고 있었다. 부하 테스트 결과 VUser 100명 기준으로 평균 응답 시간이 8.41 ms, p(95) 응답 시간이 24 ms로 측정되었다. 캐시가 없어 11,677건의 요청 중 모두 데이터베이스를 거쳐야 했으며, 이는 데이터베이스 커넥션 풀 낭비를 야기하고 동시 사용자 증가 시 성능 저하로 이어질 수 있었다.

캐싱을 적용하여 데이터베이스 SELECT 쿼리를 제거하고, 응답 시간을 3 ms 수준으로 단축하여 동시 사용자 처리 능력을 향상시키는 것을 목표로 했다.

</aside>

## 2. 성능 개선

<aside>

**개선 과정**

| 버전 | 적용 내용 | DB SELECT 쿼리 | p(95) 응답 시간 | 평균 응답 시간 | 캐시 히트율 | 총 요청 수 |
|------|---------|--------------|-----------------|------------------|-----------|----------|
| V1 | 원본 (캐싱 미적용) | 11,677 SELECT | 24 ms | 8.41 ms | 0% | 11,677 |
| V2 | 캐싱 적용 | 16 SELECT | 12 ms | 4.02 ms | 99.86% | 11,782 |

**핵심 개선 사항**

- **응답 시간 단축:** 평균 8.41 ms에서 4.02 ms로 52.2% 단축. 사용자가 체감하는 p(95) 응답 시간도 24 ms에서 12 ms로 50% 개선되어, 대부분의 사용자가 2배 이상 빠른 응답을 경험할 수 있게 되었다.

- **데이터베이스 부하 감소:** 캐싱 적용으로 SELECT 쿼리가 거의 제거되었다. 11,782건의 요청 중 데이터베이스 접근이 단 16회로 감소하여, 99.86%의 요청이 캐시에서 처리되었다.

</aside>

## 3. 적용 결과

<aside>

**최종 성과**

| 지표 | 개선 전 | 개선 후 | 개선율 |
|------|--------|--------|--------|
| p(95) 응답 시간 | 24 ms | 12 ms | 50% |
| 평균 응답 시간 | 8.41 ms | 4.02 ms | 52.2% |
| DB SELECT 쿼리 | 11,677회 | 16회 | 99.86% |
| 캐시 히트율 | 0% | 99.86% | - |

캐싱 도입으로 응답 시간이 52% 이상 단축되었으며, 데이터베이스 접근이 99% 이상 감소했다. 이제 시스템은 동시 사용자 100명 이상의 높은 부하에서도 안정적으로 약 4 ms의 빠른 응답을 유지할 수 있게 되었으며, 데이터베이스 커넥션 풀도 충분한 여유를 갖게 되어 전체 시스템의 확장성과 안정성이 크게 향상되었다.

</aside>

</details>

---

## 🚨 트러블 슈팅

<details>
<summary><b>Timezone 불일치 문제</b></summary>

### 🚨 문제 상황

경기 일정 조회 (`query_schedule`) 시 DB에 데이터가 있음에도 챗봇이 "경기가 없습니다"라고 응답하는 문제가 발생했다.

→ 로컬에서는 정상 조회되는 것을 확인

<img width="950" height="208" alt="image" src="https://github.com/user-attachments/assets/deb6af3d-82e9-4768-88c4-0bd77ac4c619" />

---

### 🔍 원인

- DB에는 UTC 기준으로 `start_time`이 저장되어 있었음
- 조회 로직은 KST (`LocalDateTime.now()`) 기준으로 실행됨
- 즉, 쿼리의 `BETWEEN` 구간이 UTC 기준과 어긋나 조회 누락 발생

**예시:**
```
KST 2025-11-04T00:00 → UTC 2025-11-03T15:00
→ 실제 DB의 UTC 데이터와 일자 불일치 발생
```

---

### 🧩 해결

조회 전 KST → UTC 변환 로직을 추가하여 DB 기준과 일치시킴

```java
private List<Match> findMatchesByKstRange(LocalDateTime startKst, LocalDateTime endKst) {
    ZoneId UTC = ZoneOffset.UTC;
    LocalDateTime startUtc = startKst.atZone(KST).withZoneSameInstant(UTC).toLocalDateTime();
    LocalDateTime endUtc = endKst.atZone(KST).withZoneSameInstant(UTC).toLocalDateTime();

    return matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(startUtc, endUtc);
}
```

**결과:**
- UTC/KST 간 시간대 일관성 확보
- 변환 후 경기 정상 조회 확인

<img width="979" height="243" alt="image" src="https://github.com/user-attachments/assets/8e698df4-2c0b-48ab-b093-61c310d2de19" />

</details>

<details>
<summary><b>tool_use_failed 오류</b></summary>

### 🚨 문제 상황

챗봇 개선 중 서버 오류(모델 툴 호출 실패)가 발생했다.

```java
"code":"tool_use_failed","failed_generation":"\\u003cfunction=query_schedule{\\"when\\":\\"오늘\\"}..."
```

---

### 🔍 원인

<details>
<summary><b>기존 프롬프트 (문제)</b></summary>

```
[툴 사용 원칙]
- 일정/승률/예측 등 사실 확인이 필요하면 제공된 툴을 사용한다.
- 한 번에 여러 툴이 필요하지 않다면 호출을 최소화한다.

[툴 사용 예시]
사용자: 오늘 경기 있어요?
도구호출: query_schedule({"when":"오늘"})
```

</details>

**문제점:**

- 위와 같이 few-shot에 적어둔 툴 호출 예시 텍스트를 AI 모델이 그대로 답변 본문에 붙여서 출력
- Groq이 `<function=...></function>`를 함수 호출 시도로 해석하려다 파싱 실패
- HTTP 400 (`tool_use_failed`)를 반환 → Spring AI가 예외 전파 → Postman/서버에서 500으로 보임

---

### 🧩 해결

<details>
<summary><b>개선된 프롬프트 (해결)</b></summary>

```
[툴 사용 원칙]
- 경기 일정이 필요하면 query_schedule 또는 query_schedule_by_date를 사용한다.
- 팀·리그의 승률이 필요하면 analyze_winning_rate를 사용한다.
- 최근 인기 있는 팀/리그를 묻는다면 query_hot_keywords를 사용한다.
- 한 번에 여러 툴이 필요하지 않다면 호출을 최소화한다.

[툴 사용 예시]
사용자: 안녕하세요! → 툴 호출 없이 최종 답만 생성
사용자: 오늘 경기 있어요? → query_schedule({"when":"오늘"})
사용자: 11월 4일 경기 일정 알려줘 → query_schedule_by_date({"month":11,"day":4})
사용자: Nexus 승률 알려줘 → analyze_winning_rate({"teamA":"Nexus"})
사용자: 요즘 인기 있는 팀은 어디야? → query_hot_keywords()
```

</details>

**해결 방법:**

- 툴 사용 원칙과 예시를 더 명확하고 직관적으로 작성
- 각 도구별 사용 시나리오를 명확하게 구분
- 예시에서 "도구호출:" 같은 텍스트를 제거하여 모델이 그대로 출력하지 않도록 개선
- 이로써 모델이 예시 텍스트를 그대로 출력하지 않고, 안정적으로 툴을 호출함

**결과:**
- 모델이 프롬프트 텍스트를 답변에 포함시키지 않음
- Groq이 정상적으로 함수 호출 파싱
- 400/500 오류 완전 해결

</details>

<details>
<summary><b>NonUniqueResultException</b></summary>

### 1. 문제 상황

nGrinder로 `/api/v1/matches/search` API 성능 테스트 실행 시 `NonUniqueResultException` 발생

```java
❌ org.springframework.dao.IncorrectResultSizeDataAccessException:
   query did not return a unique result: 2 results were returned
```

---

### 2. 원인 분석

#### 2.1 에러 스택 트레이스

```
org.springframework.dao.IncorrectResultSizeDataAccessException:
  query did not return a unique result: 2 results were returned
    at org.springframework.data.jpa.repository.query...
    at org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository.findByKeyword()
    at org.example.oddventure.domain.hotKeywords.service.HotKeywordsService.incrementSearchScore()
    at org.example.oddventure.domain.match.service.MatchService.searchMatches()
```

#### 2.2 문제 코드

```java
HotKeywords findByKeyword(String keyword);  // 단일 결과 기대
```

```java
public void incrementSearchScore(String keyword) {
    if (hotKeywordsRepository.findByKeyword(keyword) == null) {  // ← 여기서 에러 발생
        hotKeywordsRepository.save(HotKeywords.of(keyword));
    }
    redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, 1);
}
```

```java
@Transactional
public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {
    Page<MatchProjection> projections = matchRepository.searchByCondition(condition, pageable);

    hotKeywordsService.incrementSearchScore(condition.keyword());  // ← 여기서 호출

    return projections.map(MatchResponse::of);
}
```

#### 2.3 데이터베이스 상태 확인

중복 데이터 존재 확인

```sql
SELECT keyword, COUNT(*) as count
FROM hot_keywords
WHERE is_deleted = 0
GROUP BY keyword
HAVING COUNT(*) > 1;
```

**결과:**

| keyword | count |
|---------|-------|
| Europe | 2 |
| Nexus | 2 |
| Season 50 | 3 |
| SINNERS Esports | 3 |
| CCT S3 Europe Series 9 | 2 |
| bestia | 2 |
| ALGO | 2 |

#### 2.4 발생 메커니즘

<details>
<summary><b>단일 사용자 (정상)</b></summary>

```
[Thread 1]
1. findByKeyword("Europe") → null
2. save(HotKeywords("Europe")) → ID: 1
3. incrementScore("Europe", 1)
✅ 성공
```

</details>

<details>
<summary><b>동시 사용자 (Race Condition)</b></summary>

```
시간  | Thread 1              | Thread 2              | Thread 3
------|----------------------|----------------------|----------------------
T0    | findByKeyword("Europe") |                      |
T1    | → null               | findByKeyword("Europe") |
T2    |                      | → null               | findByKeyword("Europe")
T3    | save("Europe")       |                      | → null
T4    | → ID: 1 ✅           | save("Europe")       |
T5    |                      | → ID: 2 ✅           | save("Europe")
T6    |                      |                      | → ID: 3 ✅
------|----------------------|----------------------|----------------------
결과: keyword="Europe"인 레코드가 3개 생성됨 ❌

[다음 검색 요청]
T7    | findByKeyword("Europe")
T8    | → 3개 레코드 반환
T9    | ❌ NonUniqueResultException 발생!
```

</details>

**결론:**
- 단일 테스트 시 Race Condition 발생 안함
- 높은 동시성: 여러 쓰레드가 동시에 같은 키워드 검색 → Race Condition 발생

---

### 3. 해결 방법

#### 3.1 쿼리 메서드 변경

```java
public interface HotKeywordsRepository extends JpaRepository<HotKeywords, Long> {

    // ❌ 기존 (단일 결과 기대)
    // HotKeywords findByKeyword(String keyword);

    // ✅ 변경 (첫 번째 결과만 반환 + Optional)
    Optional<HotKeywords> findFirstByKeyword(String keyword);

    @Modifying
    @Query("Update HotKeywords hk SET hk.searchCount = :score WHERE hk.keyword = :keyword")
    @Transactional
    void increaseSearchCountByValue(@Param("keyword") String keyword, @Param("score") Double score);
}
```

#### 3.2 Null 체크 변경

```java
public void incrementSearchScore(String keyword) {
    // ❌ 기존
    // if (hotKeywordsRepository.findByKeyword(keyword) == null) {
    //     hotKeywordsRepository.save(HotKeywords.of(keyword));
    // }

    // ✅ 변경
    if (hotKeywordsRepository.findFirstByKeyword(keyword).isEmpty()) {
        hotKeywordsRepository.save(HotKeywords.of(keyword));
    }

    redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, 1);
}
```

---

### 4. 근본 원인 해결책

**Unique 제약 조건 추가**

```java
@Entity
@Table(name = "hot_keywords", uniqueConstraints = {
    @UniqueConstraint(columnNames = "keyword", name = "uk_hot_keywords_keyword")
})
public class HotKeywords {
    // ...
}
```

이렇게 하면 DB 레벨에서 중복을 방지하여, Race Condition 발생 시에도 안전하게 처리할 수 있다.

</details>

<details>
<summary><b>AWS SSM Client BeanCreationException</b></summary>

### 문제 상황

Spring Boot 애플리케이션이 AWS Parameter Store를 사용할 때, 로컬 환경이나 테스트 환경에서 AWS 자격 증명을 찾지 못해 SSM Client Bean 생성에 실패하는 문제가 발생.

```
❌ org.springframework.beans.factory.BeanCreationException:
   Error creating bean with name 'ssmClient'...
   Unable to find AWS credentials
```

---

### 해결 방법

#### 테스트 환경 설정 (`src/test/resources/application.yml`)

```yaml
spring:
  cloud:
    aws:
      credentials:
        access-key: test-key
        secret-key: test-secret
      region:
        static: ap-northeast-2
```

**설정 내용:**
- `access-key`: 테스트용 더미 자격 증명
- `secret-key`: 테스트용 더미 자격 증명  
- `region`: AWS 리전 설정 (ap-northeast-2는 서울)

**결과:**
- ✅ 로컬/테스트 환경에서 SSM Client Bean 정상 생성
- ✅ AWS Parameter Store 접근 불필요한 환경에서 자격 증명 에러 해결
- ✅ 프로덕션 환경에서는 실제 IAM 역할 또는 자격 증명 사용

</details>

<details>
<summary><b>캐싱 적용 후 i/o timeout 지속 발생</b></summary>

### 1. 문제 상황 (V3: 캐싱 적용 테스트)

V3 코드는 `getMatch`를 `@Cacheable`로 최적화하고, `incrementViewCount`는 DB로 호출하는 2단계 리팩토링이 적용된 상태였다.

**예상:**
- SELECT가 Redis로 대체되었으므로, DB 부하가 줄어들어 테스트가 성공할 것

**실제:**
- K6 테스트 결과 실패
- K6 로그에 `cacheHit: true` (10~30ms)가 정상적으로 찍히는 것을 확인하여 캐싱은 성공했음을 확인
- 하지만 K6 VUser가 50명 이상으로 증가하는 load_test 단계에서 i/o timeout (30초) 및 느린 응답: 30,824 ms 로그가 동일하게 발생

---

### 2. 원인 분석 (새로운 병목: UPDATE Row Lock)

K6 로그는 캐시 히트(성공)와 DB 타임아웃(실패)이 동시에 발생하고 있음을 보여주었다. 이는 `MatchController`의 두 호출 중 하나는 성공하고, 하나는 실패했다는 의미이다.

```java
// MatchController.java (V3 코드)
@GetMapping("/{matchId}")
public ResponseEntity<ApiResponse<MatchResponse>> getMatch(@PathVariable Long matchId) {
    
    // 병목 지점
    // VUser 500명이 이 메서드를 동시에 호출 (DB Write)
    matchService.incrementViewCount(matchId); 
    
    // 캐싱 성공
    // 이 메서드는 Redis에서 즉시 반환 (DB Read 없음)
    MatchResponse match = matchService.getMatch(matchId); 
    
    return ApiResponse.success(match, "매치 상세 조회에 성공했습니다.");
}
```

#### 병목 발생 메커니즘

1. `getMatch(id)`는 warmup 단계에서 캐시가 생성된 후, 모든 요청이 Redis에서 10ms 이내로 응답함 (캐싱 성공)

2. `incrementViewCount(id)`는 K6 스크립트가 5개의 ID만 반복 호출하므로, 500명의 유저(VUser)가 5개의 match 테이블 Row에 대해 동시에 UPDATE 쿼리를 실행

3. MySQL은 데이터 정합성을 위해 이 5개의 Row에 Row Lock(행 잠금)을 검

4. 500개의 요청이 10개의 DB 커넥션을 차지하고, 단 5개의 Row Lock을 획득하기 위해 경합을 벌임

5. 락을 기다리는 스레드들이 DB 커넥션을 30초 이상 점유하면서 DB 커넥션 풀이 고갈 (Timeout)되고, i/o timeout 예외가 발생

---

### 3. 해결 과정 (V4: Redis INCR 적용)

#### 의사 결정

100% 실시간 정확도가 필요하지 않고, 손실되어도 치명적이지 않은 데이터는 DB UPDATE의 Row Lock 경합을 감수할 필요가 없다고 판단. HotKeywordsService(인기 검색어)에서 이미 사용 중인 Redis INCR(원자적 증가) 방식을 도입하기로 결정.

#### 3.1 `MatchService.incrementViewCount` 수정 (V4)

```java
// ❌ 기존 (V3)
@Transactional
public void incrementViewCount(Long matchId) {
    matchRepository.incrementViewCount(matchId);  // DB UPDATE
}

// ✅ 변경 (V4)
public void incrementViewCount(Long matchId) {
    redisTemplate.opsForValue().increment("match:viewcount:" + matchId);
}
```

**변경 사항:**
- `@Transactional` 어노테이션 제거
- `matchRepository.incrementViewCount()` (DB UPDATE) 호출 로직 삭제
- `redisTemplate.opsForValue().increment()` (Redis INCR)로 변경
- 이 작업은 1ms 이내에 완료되며 DB 커넥션을 전혀 사용하지 않음

#### 3.2 `MatchScheduler` 수정 (V4)

```java
@Scheduled(fixedRate = 300000)  // 5분마다 실행
public void syncViewCountsToDB() {
    Set<String> keys = redisTemplate.keys("match:viewcount:*");
    
    for (String key : keys) {
        Long matchId = Long.parseLong(key.replace("match:viewcount:", ""));
        Long viewCount = (Long) redisTemplate.opsForValue().get(key);
        
        matchRepository.updateViewCount(matchId, viewCount);
        redisTemplate.delete(key);  // 동기화 후 삭제
    }
}
```

**변경 사항:**
- 5분마다 실행되는 `syncViewCountsToDB` 스케줄러 신설
- Redis의 `match:viewcount:*` 키를 scan하여 조회수 총합을 DB에 동기화
- 동기화 완료 후 Redis 키 삭제

---

### 4. 개선 결과 (V4 테스트)

K6로 4단계(최종) 코드를 테스트한 결과, 모든 병목 현상 해결:

**성능 지표:**

| 지표 | V1 (원본) | V4 (최종) | 개선율 |
|------|---------|---------|--------|
| i/o timeout | 발생 | 0건 | 100% 개선 |
| 평균 응답 시간 | 30,017 ms | 15 ms | 2,000배 개선 |
| TPS | 109/s | 2,450/s | **22배 향상** |
| p(95) 응답 시간 | 30,000 ms+ | 15 ms | 2,000배 개선 |

**결과:**
- ✅ i/o timeout (30초) 오류가 0건으로 사라짐
- ✅ 모든 응답이 15ms 내외로 측정됨
- ✅ TPS는 109/s (V1)에서 2,450/s (V4)로 **약 22배 향상** (테스트 환경에 따라 다를 수 있음)

</details>

<details>
<summary><b>No @Tool annotated methods found</b></summary>

### 문제 상황

```java
public AgentService(ChatClient.Builder chatClientBuilder) { 
    this.chatClient = chatClientBuilder 
        .defaultSystem("너는 e스포츠 데이터 기반 AI 챗봇이다. 사용자와의 맥락을 기억한다.") 
        .defaultToolCallbacks(aiService)
        .build(); 
}
```

**에러 메시지:**

```
Caused by: java.lang.IllegalStateException: 
No @Tool annotated methods found in org.example.oddventure.domain.ai.service.AiService@7fad58f8.
Did you mean to pass a ToolCallback or ToolCallbackProvider? 
If so, you have to use .toolCallbacks() instead of .tool()
```

AI Agent가 사용할 tool을 등록하던 도중 `@Tool` 어노테이션을 인지하지 못하는 에러가 발생

---

### 문제 원인

<details>
<summary><b>`.defaultTools()` vs `.defaultToolCallbacks()` 비교</b></summary>

| 항목 | `.defaultTools()` | `.defaultToolCallbacks()` |
|------|------------------|-------------------------|
| `@Tool` 필요? | ✅ 필요 (자동 스캔) | ❌ 필요 없음 (직접 등록) |
| Reflection 으로 메서드 찾음? | ✅ 찾음 | ❌ 찾지 않음 |
| 객체/함수? | 객체를 넣음 → 내부 메서드 스캔 | 함수/콜백을 직접 넣음 |
| 여러 Tool 메서드 지원 | ✅ 가능 | ❌ 1 callback = 1 tool |

</details>

**핵심 문제:**

`.defaultToolCallbacks()`는 tool의 존재를 스캔하려 하지 않으므로, `@Tool` 어노테이션을 인지하지 못하고 등록에 실패함

---

### 문제 해결

`.defaultToolCallbacks()` → `.defaultTools()` 로 변경

```java
public AgentService(ChatClient.Builder chatClientBuilder) { 
    this.chatClient = chatClientBuilder 
        .defaultSystem("너는 e스포츠 데이터 기반 AI 챗봇이다. 사용자와의 맥락을 기억한다.") 
        .defaultTools(aiService)  // ✅ 변경
        .build(); 
}
```

**변경 효과:**
- ✅ Spring AI가 `AiService`의 모든 `@Tool` 어노테이션된 메서드를 자동 스캔
- ✅ Reflection을 통해 메서드를 찾아 Tool로 등록
- ✅ 여러 개의 Tool 메서드를 한 번에 등록 가능

</details>

<details>
<summary><b>Helm Chart와 kubectl 리소스 충돌</b></summary>

### 📌 문제 인식

`✔kind: Prometheus`는 Helm Chart의 **템플릿 결과물**이다.

Helm chart (kube-prometheus-stack)가 내부적으로 Prometheus CRD를 생성한다.

만약 다음과 같은 YAML을 `kubectl apply`로 직접 배포하면:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: Prometheus
```

**위험한 상황:**

- ❌ Helm 상태와 실제 리소스 상태가 서로 달라짐 → Helm이 리소스를 덮어쓸 수 있음
- ❌ `helm upgrade` / `helm rollback` 시 설정이 덮어씌워질 수 있음
- ❌ Helm을 사용하는 이유가 사라진다

---

### 🧨 왜 위험한가?

Helm은 Release 상태를 저장하고 있고, CRD 리소스도 `values.yaml` 내용으로 관리하려고 한다.

하지만 `kubectl apply`로 만든 CRD 리소스를 Helm이 관리하지 않으므로, 추후 Helm 업데이트/upgrade 시 Helm은 해당 리소스를 모른다고 판단해서 Reconcile(동기화) 시도를 함 → **충돌 발생 가능**

---

### 🚩 문제 해결 과정

#### 문제 정의

**원래 적용 방식 (kubectl apply):**

```yaml
apiVersion: monitoring.coreos.com/v1
kind: Prometheus
metadata:
  name: k8s
  namespace: monitoring
spec:
  additionalScrapeConfigs:
    name: additional-scrape-configs
    key: additional-scrape-configs.yml
  serviceAccountName: prometheus-amp
  remoteWrite:
    - url: "https://aps-workspaces.ap-northeast-2.amazonaws.com/workspaces/$workspace_id/api/v1/remote_write"
      sigv4:
        region: ap-northeast-2
```

**문제:**
- Prometheus Operator를 Helm으로 배포하고 있으므로 CRD를 자동 생성 및 업데이트하고 있음
- `kubectl apply` 명령어로 리소스를 직접 수정하면서 설정의 충돌 발생

#### 가설

```
Helm과 kubectl apply 혼합 시 문제 발생
```

#### 해결 방안 검토

1. ✅ CRD YAML을 직접 apply 하지 말고, Helm `values.yml`로 관리
2. ❌ CRD를 직접 apply 하고, 대신 Helm이 그 리소스를 관리하지 않도록 설정 (복잡)
3. ❌ CRD를 GitOps로 관리 (도구 도입 번거로움, 비용)
4. ❌ Kustomize + Helm 조합 (불필요한 복잡성)

---

### ✅ 최종 해결책: Helm values.yaml에서 관리

IRSA & remote_write 설정을 **values.yaml**에 넣는 방식을 채택했다.

**이유:**
- Helm이 CRD의 desired state를 완벽하게 관리
- `helm upgrade` / `helm rollback` 가능
- 안정성과 재현성이 최고
- CRD 스펙을 정확히 알지 못할 때의 복잡한 커스텀 관리 피할 수 있음

#### 변경된 적용 방식

**prometheus-operator-values.yaml:**

```yaml
prometheus:
  prometheusSpec:
    serviceAccountName: prometheus-amp

    additionalScrapeConfigs:
      name: additional-scrape-configs
      key: additional-scrape-configs.yml

    remoteWrite:
      - url: "https://aps-workspaces.ap-northeast-2.amazonaws.com/workspaces/$workspace_id/api/v1/remote_write"
        sigv4:
          region: ap-northeast-2
```

**Helm upgrade 명령:**

```bash
helm upgrade prometheus-chart-name prometheus-community/kube-prometheus-stack \
  -n prometheus_namespace \
  -f prometheus-operator-values.yaml \
  --version current_helm_chart_version
```

| 항목 | 설명 |
|------|------|
| `helm upgrade` | 기존 Helm 설치를 업데이트하거나 없으면 설치 (`install/upgrade`) |
| `-f values.yaml` | Helm 템플릿에서 사용할 설정 파일 지정 |
| `--version` | Helm Chart 버전 지정 |

즉, **Helm 차트의 설정값을 업데이트하며 설치/배포하는 명령**이다.

---

### 📊 해결 결과

**구현 효과:**

- ✅ Prometheus Operator가 CRD (Prometheus kind)를 생성/관리
- ✅ Helm이 해당 리소스의 desired state를 관리
- ✅ `helm upgrade` / `helm rollback` 가능
- ✅ 안정성과 재현성 확보

---

### 💡 회고

Helm과 Kubernetes 직접 관리의 충돌을 경험하며 Helm의 역할을 명확히 알 수 있는 시간이었다.

**학습 내용:**
- Helm은 Kubernetes용 패키지 매니저
- Chart 안에는 Kubernetes 리소스들을 생성하기 위한 템플릿과 기본 값이 포함
- `kubectl apply`보다 Helm을 사용하면 배포, 롤백, 업그레이드 등을 간편하게 할 수 있음
- **Chart + values.yml → Helm → Kubernetes → Deployment/Service/CRD 등 생성**의 흐름을 이해함
- values.yml에 CRD 설정을 작성함으로써 Helm의 완전한 관리 하에 리소스를 유지할 수 있다

</details>

---

## 👥 팀원 소개

| 이름      | 역할  | GitHub                                    |
|---------|-----|-------------------------------------------|
| **팀장**  | 이호용 | [@github](https://github.com/nyong0313)     |
| **부팀장** | 이동찬 | [@github](https://github.com/twodc)    | 
| **팀원**  | 이동재 | [@github](https://github.com/LeeDongJae-KR)  | 
| **팀원**  | 박유현 | [@github](https://github.com/Park-yh)      | 
| **팀원**  | 정은서 | [@github](https://github.com/eunseo04) | 

