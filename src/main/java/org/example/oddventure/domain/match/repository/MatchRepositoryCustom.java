package org.example.oddventure.domain.match.repository;

import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchRepositoryCustom {
    Page<MatchProjection> searchByCondition(MatchSearchCondition condition, Pageable pageable);
}
