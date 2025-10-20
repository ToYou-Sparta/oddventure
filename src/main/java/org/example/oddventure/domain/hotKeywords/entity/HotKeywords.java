package org.example.oddventure.domain.hotKeywords.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotKeywords extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;
    private int searchCount;

    @Builder
    public HotKeywords(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
    }

    public static HotKeywords of(String keywords) {
        return HotKeywords.builder()
                .keyword(keywords)
                .build();
    }
}
