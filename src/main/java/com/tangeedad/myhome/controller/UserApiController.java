package com.tangeedad.myhome.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * UserApiController 클래스는 사용자(user) 관련 API를 제공하는 REST 컨트롤러입니다.
 * 이 컨트롤러는 사용자 조회, 생성, 수정, 삭제 및 검색 요청을 처리합니다.
 *
 * 주요 기능:
 * - 사용자 목록 조회
 * - 사용자 상세 조회
 * - 사용자 생성
 * - 사용자 수정
 * - 사용자 비활성화
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserService userService;

    /**
     * 사용자 목록 조회 API
     *
     * @param username 검색할 사용자 이름 (선택적)
     * @param method 검색 방법 (query 사용 시 데이터베이스 쿼리 활용)
     * @return 사용자 목록과 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String method) {

        List<User> users;

        try {
            if (username == null || username.isEmpty()) {
                if ("query".equals(method)) {
                    users = userService.getUserByUsernameQuery(username);
                } else {
                    users = userService.getAllUsers();
                }
            } else {
                users = userService.findUsersByUsername(username);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 특정 사용자의 상세 정보 조회 API
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 정보와 상태 코드
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 생성 API
     *
     * @param user 생성할 사용자 정보
     * @return 생성된 사용자 정보와 상태 코드
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 사용자 이름 중복
        }

        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 사용자 수정 API
     *
     * @param id 수정할 사용자 ID
     * @param userDetails 수정할 사용자 정보
     * @return 수정된 사용자 정보와 상태 코드
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        try {
            Optional<User> optionalUser = userService.getUserById(id);
            if (optionalUser.isPresent()) {
                User existingUser = optionalUser.get();
                existingUser.setUsername(userDetails.getUsername());
                existingUser.setPassword(userDetails.getPassword()); // 비밀번호 암호화 필요 시 추가 구현
                existingUser.setEnabled(userDetails.isEnabled());

                User updatedUser = userService.saveUser(existingUser);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 사용자 비활성화 API
     *
     * @param id 비활성화할 사용자 ID
     * @return 상태 코드
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setEnabled(false); // 사용자를 비활성화 처리
            userService.saveUser(existingUser);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

