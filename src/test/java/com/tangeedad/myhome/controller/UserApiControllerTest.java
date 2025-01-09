package com.tangeedad.myhome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.UserService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserApiController 단위 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserApiController userApiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userApiController).build();
    }

    /**
     * 사용자 목록 조회 테스트
     */
    @Test
    void testGetUsers() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    /**
     * 사용자 상세 조회 테스트
     */
    @Test
    void testGetUserById() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    /**
     * 사용자 생성 테스트
     */
    @Test
    void testCreateUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        when(userService.getUserByUsername("newuser")).thenReturn(null);
        when(userService.saveUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    /**
     * 사용자 수정 테스트
     */
    @Test
    void testUpdateUser() throws Exception {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("user1");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updatedUser");

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(userService.saveUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"));
    }

    /**
     * 사용자 비활성화 테스트
     */
    @Test
    void testDeactivateUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setEnabled(true);

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.saveUser(any(User.class))).thenReturn(user);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
