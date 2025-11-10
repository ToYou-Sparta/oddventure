package org.example.oddventure.domain.ai.service;

import lombok.Getter;
import org.example.oddventure.domain.ai.MatchConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.util.function.Function;

@Getter
@Service
public class AiService implements Function<AiService.Request, AiService.Response> {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final RestClient restClient;
    private final MatchConfigProperties props;

    /**
     * Constructor for initializing the AiService with configuration properties.
     *
     * @param props WeatherConfigProperties containing API URL and API key.
     */
    public AiService(MatchConfigProperties props) {
        this.props = props;
        this.restClient = RestClient.create(props.apiUrl());
    }

    /**
     * Fetches Betting information for a given city using the Betting API.
     *
     * @param bettingRequest Request object containing the city name.
     * @return Response object containing the current betting details.
     */
    @Override
    @Tool(name = "bettingAi", description = "경기 승률 및 배당 정보를 조회하는 툴입니다.")
    public Response apply(Request bettingRequest) {
        try {
            log.info("Betting Request: {}", bettingRequest);

            Response response = restClient.get()
                    .uri("/current.json?key={key}&q={q}", props.apiKey(), bettingRequest.match())
                    .retrieve() //요청을 서버에 보내고 응답을 가져옴
                    .body(Response.class); //json 응답을 response 클래스에 매핑

            log.info("Betting API Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("데이터 로딩 실패: {}", bettingRequest.match(), e);
            throw new RuntimeException("데이터 로딩에 실패했습니다. 나중에 다시 시도하세요.", e);
        }
    }


    //url이 제공하는 정보와 동일하게 이름 적기!!!!!!!!!

    /**
     * 요청 사항에서 필요한 정보들을 취합
     *
     * @param match 경기 정보
     */
    public record Request(String match) {}

    /**
     * Betting API에서 반환한 정보들을 취합해 반환
     *
     * @param matchInfo 경기 정보
     * @param betInfo 베팅 정보
     */
    public record Response(MatchInfo matchInfo, BetInfo betInfo) {}

    /**
     * @param match 경기 정보
     * @param OpposingTeam 상대팀
     * @param time 경기 시간
     */
    public record MatchInfo(String match, String OpposingTeam, LocalDateTime time) {}

    /**
     * @param odd 배당률
     * @param status 경기 진행 상태
     * @param winningRate 승률
     * @param description 설명
     */
    public record BetInfo(Long odd, String status, Long winningRate, Description description) {}

    /**
     * @param text 경기에 대한 분석을 서술
     */
    public record Description(String text) {}
}