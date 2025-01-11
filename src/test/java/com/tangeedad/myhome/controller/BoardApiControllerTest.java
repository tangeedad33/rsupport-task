package com.tangeedad.myhome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tangeedad.myhome.entity.Article;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.BoardService;
import com.tangeedad.myhome.service.FileStorageService;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import com.tangeedad.myhome.validator.ArticleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BoardApiController 단위 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class BoardApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BoardService boardService;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ArticleValidator articleValidator;

    @InjectMocks
    private BoardApiController boardApiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(boardApiController).build();
    }

    /**
     * 게시글 목록 조회 테스트
     */
    @Test
    void testGetArticles() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setStartDate(LocalDateTime.now().minusDays(1));
        article.setEndDate(LocalDateTime.now().plusDays(1));

        Page<Article> page = new PageImpl<>(Collections.singletonList(article));

        when(boardService.getArticles(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/articles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Article"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));
    }

    /**
     * 특정 게시글 상세 조회 테스트
     */
    @Test
    void testGetArticleById() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Article");
        article.setContent("Test Content");

        when(boardService.getArticleById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    /**
     * 게시글 생성 테스트
     */
    @Test
    void testCreateArticle() throws Exception {
        Article article = new Article();
        article.setTitle("New Article");
        article.setContent("New Content");

        User user = new User();
        user.setUsername("testuser");

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(article)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Sample File Content".getBytes()
        );

        when(jwtUtil.extractUsername(any())).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(boardService.saveArticle(any(Article.class))).thenReturn(article);

        mockMvc.perform(multipart("/api/articles")
                        .file(articlePart)
                        .file(filePart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Article"))
                .andExpect(jsonPath("$.content").value("New Content"));
    }

    /**
     * 게시글 수정 테스트
     */
    @Test
    void testUpdateArticle() throws Exception {
        Article existingArticle = new Article();
        existingArticle.setId(1L);
        existingArticle.setTitle("Existing Title");

        Article updatedArticle = new Article();
        updatedArticle.setId(1L);
        updatedArticle.setTitle("Updated Title");

        User user = new User();
        user.setUsername("testuser");

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updatedArticle)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "updated.txt", MediaType.TEXT_PLAIN_VALUE, "Updated File Content".getBytes()
        );

        when(jwtUtil.extractUsername(any())).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(boardService.getArticleById(1L)).thenReturn(Optional.of(existingArticle));
        when(boardService.saveArticle(any(Article.class))).thenReturn(updatedArticle);

        mockMvc.perform(multipart("/api/articles/1")
                        .file(articlePart)
                        .file(filePart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    /**
     * 게시글 삭제 테스트
     */
    @Test
    void testDeleteArticle() throws Exception {
        Mockito.doNothing().when(boardService).deleteArticle(1L);

        mockMvc.perform(delete("/api/articles/1"))
                .andExpect(status().isNoContent());
    }
}
