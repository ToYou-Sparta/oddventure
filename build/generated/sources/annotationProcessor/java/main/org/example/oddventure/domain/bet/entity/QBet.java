package org.example.oddventure.domain.bet.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBet is a Querydsl query type for Bet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBet extends EntityPathBase<Bet> {

    private static final long serialVersionUID = -1022124623L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBet bet = new QBet("bet");

    public final org.example.oddventure.common.entity.QBaseEntity _super = new org.example.oddventure.common.entity.QBaseEntity(this);

    public final NumberPath<java.math.BigDecimal> betAmount = createNumber("betAmount", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isWin = createBoolean("isWin");

    public final org.example.oddventure.domain.match.entity.QMatch match;

    public final NumberPath<java.math.BigDecimal> oddsAtBetting = createNumber("oddsAtBetting", java.math.BigDecimal.class);

    public final EnumPath<org.example.oddventure.domain.bet.enums.SelectedTeam> selectedTeam = createEnum("selectedTeam", org.example.oddventure.domain.bet.enums.SelectedTeam.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final org.example.oddventure.domain.user.entity.QUser user;

    public QBet(String variable) {
        this(Bet.class, forVariable(variable), INITS);
    }

    public QBet(Path<? extends Bet> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBet(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBet(PathMetadata metadata, PathInits inits) {
        this(Bet.class, metadata, inits);
    }

    public QBet(Class<? extends Bet> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.match = inits.isInitialized("match") ? new org.example.oddventure.domain.match.entity.QMatch(forProperty("match")) : null;
        this.user = inits.isInitialized("user") ? new org.example.oddventure.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

