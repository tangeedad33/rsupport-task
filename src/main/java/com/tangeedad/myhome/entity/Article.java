package com.tangeedad.myhome.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Articles") // 테이블 이름 명시
@Data
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min=2, max=30, message = "제목은 2자이상 30자 이하여야 합니다.")
    private String title;

    private String content;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(name = "reg_date", updatable = false, insertable = false)
    private LocalDateTime regDate;

    @Column(name = "last_update_date", updatable = false, insertable = false)
    private LocalDateTime lastUpdateDate;

    private long readCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    public void addFile(File file) {
        files.add(file);
        file.setArticle(this);
    }

    public void removeFile(File file) {
        files.remove(file);
        file.setArticle(null);
    }

    public Article() {
    }

    public Article(String title, String content, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
