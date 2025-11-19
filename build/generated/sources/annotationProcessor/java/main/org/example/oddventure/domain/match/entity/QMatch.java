package org.example.oddventure.domain.match.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMatch is a Querydsl query type for Match
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatch extends EntityPathBase<Match> {

    private static final long serialVersionUID = 345763505L;

    public static final QMatch match = new QMatch("match");

    public final org.example.oddventure.common.entity.QBaseEntity _super = new org.example.oddventure.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> fetchId = createNumber("fetchId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loser = createString("loser");

    public final StringPath matchName = createString("matchName");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final EnumPath<org.example.oddventure.domain.match.enums.MatchStatus> status = createEnum("status", org.example.oddventure.domain.match.enums.MatchStatus.class);

    public final StringPath teamA = createString("teamA");

    public final StringPath teamB = createString("teamB");

    public final NumberPath<java.math.BigDecimal> totalAmountA = createNumber("totalAmountA", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> totalAmountB = createNumber("totalAmountB", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public final StringPath winner = createString("winner");

    public QMatch(String variable) {
        super(Match.class, forVariable(variable));
    }

    public QMatch(Path<? extends Match> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMatch(PathMetadata metadata) {
        super(Match.class, metadata);
    }

}

