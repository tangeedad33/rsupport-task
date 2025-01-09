package com.tangeedad.myhome.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_files")
@Data
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnore // 순환 참조 방지
    private Article article;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기 (바이트 단위)

    @Column(name = "file_type", nullable = false)
    private String fileType; // MIME 타입

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    @Column(name = "uploaded_by", nullable = true)
    private String uploadedBy; // 업로드한 사용자 정보 (예: 사용자 이름 또는 ID)
}
