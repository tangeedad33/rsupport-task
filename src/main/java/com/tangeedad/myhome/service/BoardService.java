package com.tangeedad.myhome.service;

import com.tangeedad.myhome.entity.Article;
import com.tangeedad.myhome.entity.File;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.repository.BoardRepository;
import com.tangeedad.myhome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BoardService는 게시판 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 게시글 CRUD, 파일 저장, 사용자와 게시글의 연관 관리 등을 수행합니다.
 */
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final String uploadDir = "uploads/"; // 파일 업로드 기본 디렉토리

    @Autowired
    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    /**
     * 게시글 목록을 검색어와 페이지네이션 조건에 따라 조회합니다.
     *
     * @param searchText 검색어
     * @param pageable   페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    public Page<Article> getArticles(String searchText, Pageable pageable) {
        Pageable sortedByIdDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        LocalDateTime now = LocalDateTime.now();

        if (searchText == null || searchText.isEmpty()) {
            return boardRepository.findAllByStartDateBeforeAndEndDateAfter(now, now, sortedByIdDesc);
        }
        return boardRepository.findByTitleContainingOrContentContainingAndStartDateBeforeAndEndDateAfter(
                searchText, searchText, now, now, sortedByIdDesc
        );
    }

    /**
     * 특정 ID의 게시글을 조회하고 조회수를 증가시킵니다.
     *
     * @param id 게시글 ID
     * @return 게시글(Optional)
     */
    public Optional<Article> getArticleById(Long id) {
        Optional<Article> articleOptional = boardRepository.findById(id);
        articleOptional.ifPresent(article -> {
            article.setReadCount(article.getReadCount() + 1); // 조회수 증가
            boardRepository.save(article);
        });
        return articleOptional;
    }

    /**
     * 게시글을 저장하거나 업데이트합니다.
     *
     * @param article 저장할 게시글 객체
     * @return 저장된 게시글 객체
     */
    public Article saveArticle(Article article) {
        return boardRepository.save(article);
    }

    /**
     * 게시글을 사용자 정보와 함께 저장하며, 유효성 검증도 수행합니다.
     *
     * @param article       저장할 게시글 객체
     * @param username      작성자 이름
     * @param bindingResult 유효성 검증 결과
     */
    public void validateAndSaveArticle(Article article, String username, BindingResult bindingResult) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("로그인한 사용자 정보를 찾을 수 없습니다.");
        }

        article.setUser(user);
        boardRepository.save(article);
    }

    /**
     * 특정 ID의 게시글을 삭제합니다.
     *
     * @param id 삭제할 게시글 ID
     */
    public void deleteArticle(Long id) {
        boardRepository.deleteById(id);
    }

    /**
     * 게시글에 첨부된 파일들을 저장합니다.
     *
     * @param files   저장할 파일 목록
     * @param article 파일이 첨부될 게시글 객체
     * @return 저장된 파일 정보 리스트
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public List<File> saveFiles(List<MultipartFile> files, Article article) throws IOException {
        List<File> fileEntities = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);

                // 디렉토리 생성
                Files.createDirectories(filePath.getParent());

                // 파일 저장
                file.transferTo(filePath.toFile());

                // 파일 정보 엔티티 생성
                File fileEntity = new File();
                fileEntity.setArticle(article);
                fileEntity.setFileName(fileName);
                fileEntity.setFilePath(filePath.toString());
                fileEntities.add(fileEntity);
            }
        }
        return fileEntities;
    }
}
