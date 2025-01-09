package com.tangeedad.myhome.validator;

import com.tangeedad.myhome.entity.Article;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class ArticleValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Article.class.equals(clazz); // Article 클래스만 지원
    }
    @Override
    public void validate(Object target, Errors errors) {
        Article article = (Article) target;


        // 제목 필수 값 검사
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            errors.rejectValue("title", "title.empty", "제목을 입력하세요.");
        } else if (article.getTitle().length() < 2 || article.getTitle().length() > 30) {
            errors.rejectValue("title", "title.size", "제목은 2자 이상 30자 이하여야 합니다.");
        }

        // 내용 필수 값 검사
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            errors.rejectValue("content", "content.empty", "내용을 입력하세요.");
        }

        // startDate와 endDate 유효성 검사
        LocalDateTime now = LocalDateTime.now();

        if (article.getStartDate() != null && article.getEndDate() != null) {
            if (article.getStartDate().isAfter(article.getEndDate())) {
                errors.rejectValue("startDate", "startDate.invalid", "시작일은 종료일보다 이후일 수 없습니다.");
            }
        }

        if (article.getEndDate() != null) {
            if (article.getEndDate().isBefore(now)) {
                errors.rejectValue("endDate", "endDate.past", "종료일은 현재 시간보다 이후여야 합니다.");
            }
        }
    }
}
