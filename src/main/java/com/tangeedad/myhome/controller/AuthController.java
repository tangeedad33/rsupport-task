package com.tangeedad.myhome.controller;

import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.UserService;
import com.tangeedad.myhome.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * AuthController 클래스
 * 사용자 인증 및 회원가입 기능을 제공하는 컨트롤러입니다.
 */
@RestController
public class AuthController {

    private final UserService userService; // 사용자 관련 비즈니스 로직 처리
    private final JwtUtil jwtUtil; // JWT 토큰 생성 및 검증 유틸리티

    /**
     * AuthController 생성자
     * @param userService UserService 객체를 주입받음
     * @param jwtUtil JwtUtil 객체를 주입받음
     */
    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 로그인 API
     * @param username 사용자 이름
     * @param password 비밀번호
     * @return JWT 토큰 또는 인증 실패 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        // 사용자 이름으로 사용자 조회
        User user = userService.getUserByUsername(username);
        if (user == null || !userService.verifyPassword(password, user.getPassword())) {
            // 사용자가 없거나 비밀번호가 일치하지 않을 경우 401 Unauthorized 응답 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(username);

        // JWT 토큰을 JSON 형식으로 반환
        return ResponseEntity.ok().body(Collections.singletonMap("token", token));
    }

    /**
     * 회원가입 API
     * @param newUser 신규 사용자 정보
     * @return 회원가입 성공 또는 실패 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        try {
            // 회원가입 처리
            boolean isRegistered = userService.registerUser(newUser);
            if (!isRegistered) {
                // 사용자 이름 중복일 경우 409 Conflict 응답 반환
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }

            // 회원가입 성공 시 201 Created 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (Exception e) {
            // 서버 내부 오류 발생 시 500 Internal Server Error 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
