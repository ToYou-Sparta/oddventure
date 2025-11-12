package org.example.oddventure.support.fixture.match;

/**
 * 매치 테스트 데이터 생성을 위한 상수 클래스
 * 성능 테스트 및 개발 환경에서 사용
 */
public class MatchFixtureData {

    public static final String[] TEAMS = {
            "Betera Esports", "Johnny Speeds", "Bestia", "Bounty Hunters",
            "Sharks", "Dusty Roots", "ARCRED", "SINNERS Esports",
            "Nexus Gaming", "AMKAL ESPORTS", "VP.Priodigy", "AaB Esport",
            "K27", "Dynamo Eclot", "JieJieHao", "9BOOMPRO",
            "Phantoms", "Montne", "ALGO", "The Glecs"
    };

    public static final String[] MATCH_NAMES = {
            "CCT S3 Europe Series 9",
            "CBCS Masters Xeque Mate 2025",
            "ECL Season 50 - Cup #4 Europe",
            "CCT S3 EU Se10",
            "ECL Season 50 - Cup #4 SA",
            "Rushzone CS2 October 2025"
    };

    /**
     * 배치 저장 단위
     */
    public static final int BATCH_SIZE = 1000;

    private MatchFixtureData() {
        // 인스턴스화 방지
    }
}