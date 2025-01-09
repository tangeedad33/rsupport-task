package com.tangeedad.myhome.controller;

import com.tangeedad.myhome.entity.Article;
import com.tangeedad.myhome.entity.File;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.BoardService;
import com.tangeedad.myhome.service.FileStorageService;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import com.tangeedad.myhome.validator.ArticleValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * BoardApiController 클래스는 게시글(article) 관련 API를 제공하는 REST 컨트롤러입니다.
 * 이 컨트롤러는 게시글 조회, 생성, 수정, 삭제와 관련된 요청을 처리합니다.
 *
 * 주요 기능:
 * - 게시글 목록 조회
 * - 게시글 상세 조회
 * - 게시글 생성
 * - 게시글 수정
 * - 게시글 삭제
 */
@RestController
@RequestMapping("/api/articles")
public class BoardApiController {

    private final BoardService boardService;
    private final UserService userService;
    private final ArticleValidator articleValidator;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @Autowired
    public BoardApiController(BoardService boardService,
                              UserService userService,
                              ArticleValidator articleValidator,
                              FileStorageService fileStorageService,
                              JwtUtil jwtUtil) {
        this.boardService = boardService;
        this.userService = userService;
        this.articleValidator = articleValidator;
        this.fileStorageService = fileStorageService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 게시글 목록 조회 API
     *
     * @param searchText 검색어 (선택적)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 게시글 목록과 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<Article>> getArticles(@RequestParam(required = false) String searchText,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        try {
            List<Article> articles = boardService.getArticles(searchText, PageRequest.of(page, size)).getContent();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 게시글 상세 조회 API
     *
     * @param id 게시글 ID
     * @return 게시글 정보와 상태 코드
     */
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        try {
            Optional<Article> article = boardService.getArticleById(id);
            return article.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 게시글 생성 API
     *
     * @param article 게시글 정보 (JSON 형태)
     * @param bindingResult 유효성 검증 결과
     * @param files 첨부 파일 목록 (선택적)
     * @param authorizationHeader 인증 헤더 (JWT 토큰 포함)
     * @return 생성된 게시글 정보와 상태 코드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createArticle(
            @RequestPart("article") @Valid Article article,
            BindingResult bindingResult,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authorizationHeader) {

        articleValidator.validate(article, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            User user = userService.getUserByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
            }

            article.setUser(user);

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    String filePath = fileStorageService.storeFile(file);
                    File fileEntity = new File();
                    fileEntity.setFileName(file.getOriginalFilename());
                    fileEntity.setFileType(file.getContentType());
                    fileEntity.setFileSize(file.getSize());
                    fileEntity.setFilePath(filePath);
                    fileEntity.setUploadedBy(username);
                    fileEntity.setArticle(article);
                    article.addFile(fileEntity);
                }
            }

            Article savedArticle = boardService.saveArticle(article);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * 게시글 수정 API
     *
     * @param id 수정할 게시글 ID
     * @param article 수정된 게시글 정보
     * @param bindingResult 유효성 검증 결과
     * @param files 첨부 파일 목록 (선택적)
     * @param authorizationHeader 인증 헤더 (JWT 토큰 포함)
     * @return 수정된 게시글 정보와 상태 코드
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateArticle(
            @PathVariable Long id,
            @RequestPart("article") @Valid Article article,
            BindingResult bindingResult,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authorizationHeader) {

        articleValidator.validate(article, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            User user = userService.getUserByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
            }

            article.setUser(user);

            Optional<Article> existingArticle = boardService.getArticleById(id);
            if (existingArticle.isPresent()) {
                Article updatedArticle = existingArticle.get();
                updatedArticle.setTitle(article.getTitle());
                updatedArticle.setContent(article.getContent());
                updatedArticle.setStartDate(article.getStartDate());
                updatedArticle.setEndDate(article.getEndDate());

                if (files != null && !files.isEmpty()) {
                    for (MultipartFile file : files) {
                        String filePath = fileStorageService.storeFile(file);
                        File fileEntity = new File();
                        fileEntity.setFileName(file.getOriginalFilename());
                        fileEntity.setFileType(file.getContentType());
                        fileEntity.setFileSize(file.getSize());
                        fileEntity.setFilePath(filePath);
                        fileEntity.setUploadedBy(username);
                        fileEntity.setArticle(article);
                        updatedArticle.addFile(fileEntity);
                    }
                }

                Article savedArticle = boardService.saveArticle(updatedArticle);
                return ResponseEntity.ok(savedArticle);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 게시글 삭제 API
     *
     * @param id 삭제할 게시글 ID
     * @return 상태 코드
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            boardService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
