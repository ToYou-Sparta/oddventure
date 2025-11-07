import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const searchResponseTime = new Trend('search_response_time');

// 테스트 설정
export const options = {
    // 일정한 부하로 테스트 (데이터 볼륨 영향만 측정)
    stages: [
        {duration: '30s', target: 10},  // 0 -> 10
        {duration: '3m', target: 10},   // 10 명 유지
        {duration: '30s', target: 0},   // 10 - > 0
    ],

    thresholds: {
        'http_req_duration': [
            'p(50)<300',   // 50% 요청이 0.3초 미만 (낮은 부하, 더 엄격)
            'p(95)<800',   // 95% 요청이 0.8초 미만 (데이터 볼륨 최적화 필수)
            'p(99)<1500',  // 99% 요청이 1.5초 미만
            'max<3000'     // 최대 응답 시간 3초 미만 (5초→3초 강화)
        ],
        'http_req_failed': ['rate<0.005'],  // 실패율 0.5% 미만 (1%→0.5% 강화)
        'errors': ['rate<0.005'],            // 에러율 0.5% 미만
        'search_response_time': ['p(95)<800'],  // 커스텀 메트릭 임계값
    },
};

const BASE_URL = 'http://host.docker.internal:8080';
const SEARCH_ENDPOINT = `${BASE_URL}/api/v1/matches/search`;

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

// 랜덤 검색 생성
function generateSearchCondition() {
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];

    const now = new Date();
    const fromDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000); // 1달 전
    const toDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);   // 1달 후

    return {
        keyword: keyword,
        fromDate: fromDate.toISOString(),
        toDate: toDate.toISOString(),
    };
}

export default function () {
    const searchCondition = generateSearchCondition();

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            name: 'DataVolumeTest',
        },
    };

    const page = Math.floor(Math.random() * 3); // 0~2 페이지
    const size = 10;
    const url = `${SEARCH_ENDPOINT}?page=${page}&size=${size}`;

    const startTime = new Date();
    const response = http.post(url, JSON.stringify(searchCondition), params);
    const endTime = new Date();
    const duration = endTime - startTime;

    // 응답 검증
    const checkRes = check(response, {
        '상태 코드 200': (r) => r.status === 200,
        '데이터 존재': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined;
            } catch (e) {
                return false;
            }
        },
        '유효한 JSON 응답': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
    });

    // 메트릭 기록
    errorRate.add(!checkRes);
    searchResponseTime.add(duration);

    // 느린 쿼리 로깅 (1초 이상)
    if (duration > 1000) {
        console.log(`경고: 느린 쿼리 감지 ${duration}ms (키워드: "${searchCondition.keyword}", 페이지: ${page})`);
    }

    // 에러 로깅
    if (response.status !== 200) {
        console.log(`에러: 상태 코드 ${response.status} (키워드: "${searchCondition.keyword}", 페이지: ${page})`);
    }

    sleep(Math.random() * 2 + 1);
}

export function setup() {
    console.log('=== 시나리오 1: 데이터 볼륨 영향도 테스트 ===');
    console.log(`테스트 대상 API: ${BASE_URL}`);
    console.log('이 테스트는 데이터 양이 증가할 때 성능 저하를 측정합니다');
    return {startTime: new Date().toISOString()};
}

export function teardown(data) {
    console.log('=== 테스트 완료 ===');
    console.log(`시작 시간: ${data.startTime}`);
    console.log(`종료 시간: ${new Date().toISOString()}`);
}