package com.tangeedad.myhome.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    /**
     * FileStorageService 생성자.
     * 파일 저장 디렉토리를 설정하고 디렉토리를 생성합니다.
     */
    public FileStorageService() {
        // 업로드 파일 저장 디렉토리 설정
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            // 디렉토리가 없으면 생성
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * MultipartFile을 저장하고 저장된 파일의 경로를 반환합니다.
     *
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일의 경로
     */
    public String storeFile(MultipartFile file) {
        // 파일 이름을 가져오고 클린 업
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 파일 이름에 '..'가 포함되어 있으면 에러 발생
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // 저장 경로 설정
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            // 파일 저장
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
