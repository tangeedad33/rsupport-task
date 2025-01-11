package com.tangeedad.myhome.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangeedad.myhome.entity.Article;
import com.tangeedad.myhome.entity.Role;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.BoardService;
import com.tangeedad.myhome.service.RoleService;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoardApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;

    @BeforeEach
    void setup() {
        // 초기화 전 데이터 클리어
        boardService.getArticles(null, PageRequest.of(0, Integer.MAX_VALUE)) // Pageable 객체 추가
                .forEach(article -> boardService.deleteArticle(article.getId()));
        userService.getAllUsers().forEach(user -> userService.deleteUserById(user.getId()));
        roleService.getAllRoles().forEach(role -> roleService.deleteRole(role.getId()));

        // 기본 역할 생성
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        roleService.saveRole(userRole);

        // 사용자 등록
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass"); // 암호화 전 비밀번호 설정
        user.setRoles(roleService.getAllRoles());
        userService.saveUser(user);

        // JWT 토큰 발급
        jwtToken = "Bearer " + jwtUtil.generateToken("testuser");
    }

    @Test
    void testCreateAndGetArticles() throws Exception {
        // 사용자 생성
        User user = userService.getUserByUsername("testuser");

        // 게시글 데이터 생성
        Article article = new Article();
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setStartDate(LocalDateTime.now().minusDays(1));
        article.setEndDate(LocalDateTime.now().plusDays(1));
        article.setUser(user); // 사용자 설정 추가

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(article)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Sample File Content".getBytes()
        );

        // 게시글 생성 요청
        mockMvc.perform(multipart("/api/articles")
                        .file(articlePart)
                        .file(filePart)
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    @Test
    void testUpdateArticle() throws Exception {
        // 사용자 생성
        User user = userService.getUserByUsername("testuser");

        // 게시글 생성
        Article article = new Article();
        article.setTitle("Original Title");
        article.setContent("Original Content");
        article.setUser(user);
        article.setStartDate(LocalDateTime.now().minusDays(1));
        article.setEndDate(LocalDateTime.now().plusDays(1));
        Article savedArticle = boardService.saveArticle(article);

        // 수정할 데이터 준비
        Article updatedArticle = new Article();
        updatedArticle.setTitle("Updated Title");
        updatedArticle.setContent("Updated Content");
        updatedArticle.setStartDate(LocalDateTime.now().minusDays(2));
        updatedArticle.setEndDate(LocalDateTime.now().plusDays(2));

        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "article.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updatedArticle)
        );

        // 게시글 수정 요청
        mockMvc.perform(multipart("/api/articles/" + savedArticle.getId())
                        .file(articlePart)
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testDeleteArticle() throws Exception {
        // 사용자 생성
        User user = userService.getUserByUsername("testuser");

        // 게시글 생성
        Article article = new Article();
        article.setTitle("Article to Delete");
        article.setContent("Content to Delete");
        article.setUser(user);
        article.setStartDate(LocalDateTime.now().minusDays(1));
        article.setEndDate(LocalDateTime.now().plusDays(1));
        Article savedArticle = boardService.saveArticle(article);

        // 게시글 삭제 요청
        mockMvc.perform(delete("/api/articles/" + savedArticle.getId())
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isNoContent());

        // 삭제 확인
        assertThat(boardService.getArticleById(savedArticle.getId())).isEmpty();
    }
}
