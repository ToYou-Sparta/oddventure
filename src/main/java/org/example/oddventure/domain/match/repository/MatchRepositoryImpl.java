package org.example.oddventure.domain.match.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.entity.QMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

public class MatchRepositoryImpl implements MatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MatchRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MatchProjection> searchByCondition(MatchSearchCondition condition, Pageable pageable) {

        QMatch match = QMatch.match;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(condition.keyword())) {
            builder.and(match.matchName.containsIgnoreCase(condition.keyword())
                    .or(match.teamA.containsIgnoreCase(condition.keyword()))
                    .or(match.teamB.containsIgnoreCase(condition.keyword())));
        }

        if (condition.fromDate() != null) {
            builder.and(match.startTime.goe(condition.fromDate()));
        }

        if (condition.toDate() != null) {
            builder.and(match.startTime.loe(condition.toDate()));
        }

        List<MatchProjection> matches = queryFactory.select(
                        Projections.constructor(MatchProjection.class,
                                match.id,
                                match.matchName,
                                match.teamA,
                                match.teamB,
                                match.totalAmountA,
                                match.totalAmountB,
                                match.startTime,
                                match.endTime,
                                match.status,
                                match.winner,
                                match.viewCount,
                                match.createdAt))
                .from(match)
                .where(builder)
                .orderBy(match.startTime.asc(), match.matchName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(match.count()).from(match).where(builder);

        return PageableExecutionUtils.getPage(matches, pageable, countQuery::fetchOne);
    }
}
