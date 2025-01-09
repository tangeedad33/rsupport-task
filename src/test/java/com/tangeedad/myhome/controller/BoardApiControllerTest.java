package com.tangeedad.myhome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangeedad.myhome.entity.Article;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.BoardService;
import com.tangeedad.myhome.service.FileStorageService;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import com.tangeedad.myhome.validator.ArticleValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BoardApiController.class)
@Import(BoardApiControllerTest.TestConfig.class)
class BoardApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ArticleValidator articleValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestConfig {
        @Bean
        public BoardService boardService() {
            return Mockito.mock(BoardService.class);
        }

        @Bean
        public FileStorageService fileStorageService() {
            return Mockito.mock(FileStorageService.class);
        }

        @Bean
        public ArticleValidator articleValidator() {
            return new ArticleValidator();
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }

        @Bean
        public BoardApiController boardApiController(BoardService boardService,
                                                     UserService userService,
                                                     ArticleValidator articleValidator,
                                                     FileStorageService fileStorageService,
                                                     JwtUtil jwtUtil) {
            return new BoardApiController(boardService, userService, articleValidator, fileStorageService, jwtUtil);
        }
    }

    @Test
    @WithMockUser(username = "user1")
    void testGetArticles() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Title");
        article.setContent("Test Content");

        Page<Article> page = new PageImpl<>(Collections.singletonList(article));

        Mockito.when(boardService.getArticles(any(String.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/articles")
                        .param("searchText", "Test")
                        .param("page", "0")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Title"));
    }

    @Test
    @WithMockUser(username = "user1")
    void testGetArticleById() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Title");
        article.setContent("Test Content");

        Mockito.when(boardService.getArticleById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/articles/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @WithMockUser(username = "user1")
    void testCreateArticleWithValidData() throws Exception {
        String token = "mocked-jwt-token";

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new Article(
                        "Valid Title",
                        "Valid Content",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1))
                )
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Sample File Content".getBytes()
        );

        Article savedArticle = new Article();
        savedArticle.setId(1L);
        savedArticle.setTitle("Valid Title");
        savedArticle.setContent("Valid Content");

        Mockito.when(fileStorageService.storeFile(any())).thenReturn("stored/test.txt");
        Mockito.when(boardService.saveArticle(any(Article.class))).thenReturn(savedArticle);
        Mockito.when(jwtUtil.extractUsername(token)).thenReturn("user1");
        Mockito.when(userService.getUserByUsername("user1")).thenReturn(new User());

        mockMvc.perform(multipart("/api/articles")
                        .file(articlePart)
                        .file(filePart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Valid Title"));
    }

    @Test
    @WithMockUser(username = "user1")
    void testUpdateArticleWithValidData() throws Exception {
        String token = "mocked-jwt-token";

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new Article(
                        "Updated Title",
                        "Updated Content",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1))
                )
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "updated.txt", MediaType.TEXT_PLAIN_VALUE, "Updated File Content".getBytes()
        );

        Article existingArticle = new Article();
        existingArticle.setId(1L);
        existingArticle.setTitle("Old Title");
        existingArticle.setContent("Old Content");

        Article updatedArticle = new Article();
        updatedArticle.setId(1L);
        updatedArticle.setTitle("Updated Title");
        updatedArticle.setContent("Updated Content");

        Mockito.when(boardService.getArticleById(1L)).thenReturn(Optional.of(existingArticle));
        Mockito.when(fileStorageService.storeFile(any())).thenReturn("stored/updated.txt");
        Mockito.when(boardService.saveArticle(any(Article.class))).thenReturn(updatedArticle);
        Mockito.when(jwtUtil.extractUsername(token)).thenReturn("user1");
        Mockito.when(userService.getUserByUsername("user1")).thenReturn(new User());

        mockMvc.perform(multipart("/api/articles/1")
                        .file(articlePart)
                        .file(filePart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "user1")
    void testDeleteArticle() throws Exception {
        Mockito.doNothing().when(boardService).deleteArticle(1L);

        mockMvc.perform(delete("/api/articles/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
