import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

const chatErrorRate = new Rate('chat_errors');
const chatResponseTime = new Trend('chat_response_time');

const BASE_URL = 'http://host.docker.internal:8080';
const CHAT_ENDPOINT = `${BASE_URL}/api/v1/chat/loadtest`;

export const options = {
    stages: [
        {duration: '1m', target: 100},
        {duration: '1m', target: 300},
        {duration: '3m', target: 500},
        {duration: '30s', target: 0}
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<2000'],
        chat_errors: ['rate<0.05'],
        chat_response_time: ['p(95)<2000']
    },
};

export default function () {
    const payload = JSON.stringify({
        message: 'Virtual Thread 부하 테스트 중입니다.'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json'
        },
        tags: {
            name: 'ChatVirtualThreadTest'
        },
    };

    const start = Date.now();
    const res = http.post(CHAT_ENDPOINT, payload, params);
    const duration = Math.max(0, Date.now() - start);

    const ok = check(res, {
        'status is 200': (r) => r.status === 200,
    });

    chatErrorRate.add(!ok);
    chatResponseTime.add(duration);

    sleep(0.5);
}
