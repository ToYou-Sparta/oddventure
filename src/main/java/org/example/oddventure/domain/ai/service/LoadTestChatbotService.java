package org.example.oddventure.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoadTestChatbotService {

    private static final long LATENCY_MS = 500L;

    public String reply(String userMessage) {
        try {
            Thread.sleep(LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "[loadtest] 실제 Groq 호출 없이 생성된 테스트 응답입니다. :" + userMessage;
    }
}
