package org.example.oddventure.domain.hotKeywords.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHotKeywords is a Querydsl query type for HotKeywords
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHotKeywords extends EntityPathBase<HotKeywords> {

    private static final long serialVersionUID = -1213610511L;

    public static final QHotKeywords hotKeywords = new QHotKeywords("hotKeywords");

    public final org.example.oddventure.common.entity.QBaseEntity _super = new org.example.oddventure.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final NumberPath<Integer> searchCount = createNumber("searchCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHotKeywords(String variable) {
        super(HotKeywords.class, forVariable(variable));
    }

    public QHotKeywords(Path<? extends HotKeywords> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHotKeywords(PathMetadata metadata) {
        super(HotKeywords.class, metadata);
    }

}

