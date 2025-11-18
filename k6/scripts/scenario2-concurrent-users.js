import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend, Counter} from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const timeoutRate = new Rate('timeouts'); //동시성 테스트에서는 과부하로 타임아웃 발생 가능성 있음
const searchResponseTime = new Trend('search_response_time');
const slowQueries = new Counter('slow_queries'); // 2초 이상 응답을 카운트

// 동시 사용자 수 설정 (환경 변수로 전달)
const TARGET_VUS = __ENV.TARGET_VUS ? parseInt(__ENV.TARGET_VUS) : 50;

export const options = {
    stages: [
        {duration: '1m', target: TARGET_VUS},   // 0 -> 10
        {duration: '3m', target: TARGET_VUS},   // 10 유지
        {duration: '30s', target: 0},           // 10 -> 0
    ],

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
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const API_URL = __ENV.API_URL || '/api/v1/matches/search';
const ENDPOINT = BASE_URL + API_URL;

// 다양한 검색 키워드 (실제 테스트 데이터와 매칭)
const keywords = [
    // 정확한 팀명 매칭
    'Bestia',
    'Johnny Speeds',
    'Sharks',
    'SINNERS Esports',
    'ALGO',
    'Montne',
    'VP.Priodigy',

    // 부분 팀명 매칭
    'Johnny',
    'Esports',
    'Gaming',
    'Hunters',
    'Eclot',

    // 정확한 매치명 매칭
    'CCT S3 Europe Series 9',
    'CBCS Masters Xeque Mate 2025',
    'ECL Season 50 - Cup #4 Europe',

    // 부분 매치명 매칭 (많은 결과 예상)
    'CCT',
    'ECL',
    'Season 50',
    'Europe',
    'Rushzone',
    'Cup',

    // 짧은 키워드 (매우 많은 결과)
    'S3',
    '2025',

    // 대소문자
    'arcred',
    'MONTNE',

    // 빈 문자열 (전체 검색)
    '',
];

// 로그인 토큰 획득
function getAuthToken() {
    const loginResponse = http.post(`${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            email: 'hello@naver.com',
            password: 'hello123!@#'
        }),
        {headers: {'Content-Type': 'application/json'}}
    );

    if (loginResponse.status !== 200) {
        console.error(`로그인 실패: 상태 코드 ${loginResponse.status}`);
        console.error(`응답 내용: ${loginResponse.body}`);
        throw new Error(`로그인 실패: ${loginResponse.status}`);
    }

    const body = JSON.parse(loginResponse.body);

    if (!body.data || !body.data.accessToken) {
        console.error(`토큰이 응답에 없습니다: ${JSON.stringify(body)}`);
        throw new Error('accessToken을 찾을 수 없습니다');
    }

    return body.data.accessToken;
}

function generateSearchCondition() {
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];

    const now = new Date();
    const fromDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    const toDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);

    return {
        keyword: keyword,
        fromDate: fromDate.toISOString(),
        toDate: toDate.toISOString(),
    };
}

export default function (data) {
    const searchCondition = generateSearchCondition();

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`
        },
        tags: {
            name: 'ConcurrentUsersTest',
            vus: TARGET_VUS.toString(),
        },
        timeout: '30s',  // 30초 타임아웃
    };

    const page = Math.floor(Math.random() * 5);
    const size = 10;
    const url = `${ENDPOINT}?page=${page}&size=${size}`;

    const startTime = new Date();
    const response = http.post(url, JSON.stringify(searchCondition), params);
    const endTime = new Date();
    const duration = endTime - startTime;

    // 타임아웃 체크
    const isTimeout = duration > 30000;
    timeoutRate.add(isTimeout);

    // 느린 쿼리 카운트 (2초 이상)
    if (duration > 2000) {
        slowQueries.add(1);
    }

    // 응답 검증
    const checkRes = check(response, {
        '상태 코드 200': (r) => r.status === 200,
        '타임아웃 없음': (r) => !isTimeout,
        '응답 시간 허용 범위': (r) => duration < 5000,
        '데이터 존재': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined;
            } catch (e) {
                return false;
            }
        },
    });

    // 메트릭 기록
    errorRate.add(!checkRes);
    searchResponseTime.add(duration);

    // 심각한 성능 저하 로깅
    if (duration > 5000) {
        console.log(`심각: 응답 시간 ${duration}ms (${TARGET_VUS} VUs, 키워드: "${searchCondition.keyword}")`);
    }

    // 에러 로깅
    if (response.status !== 200) {
        console.log(`에러: 상태 코드 ${response.status} (${TARGET_VUS} VUs)`);
    }

    sleep(Math.random() * 1.5 + 0.5);
}

export function setup() {
    console.log('=== 시나리오 2: 동시 사용자 부하 테스트 ===');
    console.log(`목표 가상 사용자 수: ${TARGET_VUS}`);
    console.log(`대상 API: ${ENDPOINT}`);
    console.log('이 테스트는 동시 부하 상황에서 확장성과 성능 저하를 측정합니다');

    const token = getAuthToken();

    return {
        startTime: new Date().toISOString(),
        targetVUs: TARGET_VUS,
        token: token
    };
}

export function teardown(data) {
    console.log('=== 테스트 완료 ===');
    console.log(`목표 가상 사용자 수: ${data.targetVUs}`);
    console.log(`시작 시간: ${data.startTime}`);
    console.log(`종료 시간: ${new Date().toISOString()}`);
}