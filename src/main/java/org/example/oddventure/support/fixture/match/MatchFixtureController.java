package org.example.oddventure.support.fixture.match;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매치 테스트 데이터 생성 컨트롤러
 * 성능 테스트 및 개발 환경에서만 활성화
 */
@Profile({"local", "dev"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixture/matches")
public class MatchFixtureController {

    private final MatchFixtureService matchFixtureService;

    /**
     * 성능 테스트용 대량 더미 데이터 생성
     * 개발 환경에서만 사용
     */
    @PostMapping("/test-data")
    public ResponseEntity<ApiResponse<String>> generateTestData(
            @RequestParam int count
    ) {
        long startTime = System.currentTimeMillis();
        matchFixtureService.generateTestData(count);
        long duration = System.currentTimeMillis() - startTime;

        return ApiResponse.success(
                String.format("%d개의 테스트 데이터 생성 완료 (소요시간: %dms)", count, duration),
                "테스트 데이터가 생성되었습니다."
        );
    }
}