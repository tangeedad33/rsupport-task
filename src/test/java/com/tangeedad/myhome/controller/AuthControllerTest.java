package com.tangeedad.myhome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController의 단위 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    /**
     * 로그인 테스트 - 성공 시 JWT 토큰 반환
     */
    @Test
    void testLoginSuccess() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedpassword");

        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(userService.verifyPassword("password", "encodedpassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    /**
     * 로그인 테스트 - 실패 시 401 응답 반환
     */
    @Test
    void testLoginFailure() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 회원가입 테스트 - 성공 시 201 응답 반환
     */
    @Test
    void testRegisterSuccess() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        when(userService.registerUser(any(User.class))).thenReturn(true);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("User registered successfully"));
    }

    /**
     * 회원가입 테스트 - 사용자 이름 중복 시 409 응답 반환
     */
    @Test
    void testRegisterConflict() throws Exception {
        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("password");

        when(userService.registerUser(any(User.class))).thenReturn(false);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Username already exists"));
    }

    /**
     * 회원가입 테스트 - 서버 오류 시 500 응답 반환
     */
    @Test
    void testRegisterServerError() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        when(userService.registerUser(any(User.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Unexpected error"));
    }
}
