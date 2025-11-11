import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const cacheHitRate = new Rate('cache_hits');
const responseTime = new Trend('match_detail_response_time');
const dbAccessCount = new Counter('db_access_count');

// 테스트 설정
export const options = {
    scenarios: {
        // 시나리오 1: 워밍업 (캐시 채우기)
        warmup: {
            executor: 'constant-vus',
            vus: 5,
            duration: '30s',
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        // 시나리오 2: 실제 부하 테스트 (캐시 효과 측정)
        load_test: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                { duration: '30s', target: 10 },  // 10명 유지
                { duration: '1m', target: 50 },   // 50명으로 증가
                { duration: '1m', target: 50 },   // 50명 유지
                { duration: '1m', target: 100 },  // 100명으로 증가
                { duration: '1m', target: 100 },  // 100명 유지
                { duration: '30s', target: 0 },   // 0명으로 감소
            ],
            startTime: '30s',
            tags: { phase: 'load_test' },
        },
    },

    thresholds: {
        // 응답 시간 임계값 (캐싱 적용 시 크게 개선되어야 함)
        'match_detail_response_time': [
            'p(50)<100',   // 중앙값 100ms 미만
            'p(95)<300',   // 95% 300ms 미만
            'p(99)<500',   // 99% 500ms 미만
            'max<2000'     // 최대 2초 미만
        ],

        // 에러율 임계값
        'errors': ['rate<0.01'], // 1% 미만

        // HTTP 실패율
        'http_req_failed': ['rate<0.01'],
    },
};

// 환경 변수에서 API URL 가져오기
const BASE_URL = __ENV.API_URL || 'http://host.docker.internal:8080';
const MATCH_DETAIL_ENDPOINT = `${BASE_URL}/api/v1/matches`;

// 테스트할 경기 ID 목록 (FINISHED 상태의 경기들)
// 실제 DB에 있는 종료된 경기 ID로 변경해야 함
const FINISHED_MATCH_IDS = [
    1, 2, 3, 4, 5
];

// 메인 테스트 함수
export default function () {
    // 종료된 경기 중 랜덤 선택 (캐시 히트율을 높이기 위해)
    const matchId = FINISHED_MATCH_IDS[Math.floor(Math.random() * FINISHED_MATCH_IDS.length)];
    const url = `${MATCH_DETAIL_ENDPOINT}/${matchId}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            name: 'MatchDetail',
            match_id: matchId.toString(),
        },
    };

    const startTime = new Date();
    const response = http.get(url, params);
    const endTime = new Date();
    const duration = endTime - startTime;

    // 응답 검증
    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'has data': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined && body.data !== null;
            } catch (e) {
                return false;
            }
        },
        'match status is FINISHED': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data.status === 'FINISHED';
            } catch (e) {
                return false;
            }
        },
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 메트릭 기록
    errorRate.add(!checkRes);
    responseTime.add(duration);

    // 캐시 히트 추정 (응답 시간이 빠르면 캐시 히트로 간주)
    // Redis 캐시 히트 시 일반적으로 10~50ms
    const isCacheHit = duration < 50;
    cacheHitRate.add(isCacheHit ? 1 : 0);

    if (!isCacheHit) {
        dbAccessCount.add(1);
    }

    // 응답 시간별 로깅 (100번째 요청마다)
    if (__ITER % 100 === 0) {
        console.log(`[Iter ${__ITER}] matchId: ${matchId}, duration: ${duration}ms, cacheHit: ${isCacheHit}`);
    }

    // 느린 쿼리 감지
    if (duration > 500) {
        console.log(`느린 응답: ${duration}ms, matchId: ${matchId}`);
    }

    // 사용자 행동 시뮬레이션 (1~3초 대기)
    sleep(Math.random() * 2 + 1);
}

// 테스트 시작 시 실행
export function setup() {
    console.log('=== 캐싱 성능 비교 테스트 시작 ===');
    console.log(`테스트 대상 API: ${BASE_URL}`);
    console.log(`종료된 경기 수: ${FINISHED_MATCH_IDS.length}개`);
    console.log('');
    console.log('테스트 단계:');
    console.log('1. 워밍업 (30초): 캐시 채우기');
    console.log('2. 부하 테스트 (5분): 캐시 효과 측정');
    console.log('   - 10 VUs → 50 VUs → 100 VUs 단계별 증가');
    console.log('');
    console.log('측정 항목:');
    console.log('- 응답 시간 (p50, p95, p99)');
    console.log('- 캐시 히트율');
    console.log('- DB 접근 횟수');
    console.log('- 에러율');
    console.log('');

    return { startTime: new Date().toISOString() };
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('');
    console.log('=== 테스트 완료 ===');
    console.log(`시작 시간: ${data.startTime}`);
    console.log(`종료 시간: ${new Date().toISOString()}`);
    console.log('');
    console.log('결과 요약:');
    console.log('- Grafana 대시보드에서 상세 메트릭 확인');
    console.log('- 캐시 적용 전후 응답 시간 비교');
    console.log('- DB 커넥션 풀 사용량 비교');
    console.log('- JVM 메모리 사용량 비교');
    console.log('');
    console.log('결과 파일 위치:');
    console.log('- JSON: k6/results/scenario-cache-comparison-{VUS}vus-result.json');
    console.log('- Summary: k6/results/scenario-cache-comparison-{VUS}vus-summary.json');
}