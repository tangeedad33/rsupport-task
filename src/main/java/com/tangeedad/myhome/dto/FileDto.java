package com.tangeedad.myhome.dto;

import com.tangeedad.myhome.entity.File;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileDto {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private String uploadedBy;

    public FileDto() {
    }

    public FileDto(File file) {
        this.id = file.getId();
        this.fileName = file.getFileName();
        this.filePath = file.getFilePath();
        this.fileSize = file.getFileSize();
        this.fileType = file.getFileType();
        this.uploadDate = file.getUploadDate();
        this.uploadedBy = file.getUploadedBy();
    }
}
