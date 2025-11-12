package org.example.oddventure.domain.match.document;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.example.oddventure.domain.match.entity.Match;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Elasticsearch에 저장되는 Match 문서
 * MySQL의 Match 엔티티랑 별도
 */

@Document(indexName = "matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long fetchId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String matchName;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String teamA;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String teamB;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmountA;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmountB;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime startTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime endTime;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String winner;

    @Field(type = FieldType.Keyword)
    private String loser;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    /**
     * MySQL Match 엔티티를 Elasticsearch Document로 변환
     */
    public static MatchDocument from(Match match) {
        return MatchDocument.builder()
                .id(String.valueOf(match.getId()))  // MySQL ID를 문자열로 변환
                .fetchId(match.getFetchId())
                .matchName(match.getMatchName())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .totalAmountA(match.getTotalAmountA())
                .totalAmountB(match.getTotalAmountB())
                .startTime(match.getStartTime())
                .endTime(match.getEndTime())
                .status(match.getStatus().name())
                .winner(match.getWinner())
                .loser(match.getLoser())
                .viewCount(match.getViewCount())
                .createdAt(match.getCreatedAt())
                .build();
    }

}
