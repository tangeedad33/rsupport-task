package com.tangeedad.myhome.dto;

import com.tangeedad.myhome.entity.Article;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ArticleDisplayDto {
    private Long id; // 게시글 ID
    private String title; // 제목
    private String content; // 내용
    private LocalDateTime regDate; // 등록일
    private long readCount; // 조회수
    private String userName; // 작성자 이름

    public ArticleDisplayDto() {
    }

    // Article 엔티티를 받아 필요한 필드만 설정하는 생성자
    public ArticleDisplayDto(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.regDate = article.getRegDate();
        this.readCount = article.getReadCount();
        this.userName = article.getUser() != null ? article.getUser().getUsername() : null; // 작성자 이름
    }
}
