package com.tangeedad.myhome.repository;

import com.tangeedad.myhome.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Article, Long> {
    // 제목에 특정 문자열이 포함된 Article 검색
    Page<Article> findAllByStartDateBeforeAndEndDateAfter(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Article> findByTitleContainingOrContentContainingAndStartDateBeforeAndEndDateAfter(
            String title, String content, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
