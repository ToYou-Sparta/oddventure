package org.example.oddventure.domain.hotKeywords.repository;

import org.example.oddventure.domain.hotKeywords.entity.HotKeywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface HotKeywordsRepository extends JpaRepository<HotKeywords, Long> {

    HotKeywords findByKeyword(String keyword);

    @Modifying // 업데이트 쿼리를 실행하기 위한 JPA어노테이션
    @Query("Update HotKeywords hk SET hk.searchCount = :score WHERE hk.keyword = :keyword")
    //매개변수로 준 keyword에 해당하는 keyword 조회수칼럼을 매개변수로 준 score로 업데이트한다는 말
    @Transactional
    void increaseSearchCountByValue(@Param("keyword") String keyword, @Param("score") Double score);
}
