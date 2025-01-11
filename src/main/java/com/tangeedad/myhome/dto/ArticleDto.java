package com.tangeedad.myhome.dto;

import com.tangeedad.myhome.entity.Article;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime regDate;
    private LocalDateTime lastUpdateDate;
    private long readCount;
    private UserDto user; // User 정보를 포함
    private List<FileDto> files; // 파일 리스트 DTO

    public ArticleDto()  {
    }

    public ArticleDto(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.startDate = article.getStartDate();
        this.endDate = article.getEndDate();
        this.regDate = article.getRegDate();
        this.lastUpdateDate = article.getLastUpdateDate();
        this.readCount = article.getReadCount();
        this.user = article.getUser() != null ? new UserDto(article.getUser()) : null;
        this.files = article.getFiles().stream()
                .map(FileDto::new)
                .collect(Collectors.toList());
    }
}
