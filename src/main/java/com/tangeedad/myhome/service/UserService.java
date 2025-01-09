package com.tangeedad.myhome.service;

import com.tangeedad.myhome.entity.Role;
import com.tangeedad.myhome.entity.User;
import com.tangeedad.myhome.repository.RoleRepository;
import com.tangeedad.myhome.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserService는 사용자 관리와 관련된 주요 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자 생성, 조회, 삭제, 비밀번호 암호화, 역할 할당 등을 수행합니다.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 모든 사용자를 조회합니다.
     *
     * @return 사용자 목록
     */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("사용자 조회에 실패했습니다.", e);
        }
    }

    /**
     * 사용자 이름으로 검색된 사용자 목록을 반환합니다.
     *
     * @param username 검색할 사용자 이름
     * @return 검색된 사용자 목록
     */
    public List<User> findUsersByUsername(String username) {
        try {
            return userRepository.findByUsernameContaining(username);
        } catch (Exception e) {
            throw new RuntimeException("사용자 이름으로 검색 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 이름으로 JPQL 쿼리를 실행하여 사용자 목록을 반환합니다.
     *
     * @param username 검색할 사용자 이름
     * @return 검색된 사용자 목록
     */
    public List<User> getUserByUsernameQuery(String username) {
        try {
            return userRepository.findByUsernameQuery(username);
        } catch (Exception e) {
            throw new RuntimeException("사용자 이름으로 JPQL 쿼리 실행 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ID를 기준으로 사용자 정보를 조회합니다.
     *
     * @param id 사용자 ID
     * @return Optional 객체에 감싸진 사용자 정보
     */
    public Optional<User> getUserById(Long id) {
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("ID를 기준으로 사용자 정보를 조회하는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자를 저장하거나 업데이트합니다.
     *
     * @param user 저장할 사용자 객체
     * @return 저장된 사용자 객체
     */
    public User saveUser(@Valid User user) {
        try {
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("사용자 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 ID를 기준으로 사용자를 삭제합니다.
     *
     * @param id 삭제할 사용자 ID
     */
    public void deleteUserById(Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
            } else {
                throw new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다: " + id);
            }
        } catch (Exception e) {
            throw new RuntimeException("사용자 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 ID로 해당 사용자가 존재하는지 확인합니다.
     *
     * @param id 확인할 사용자 ID
     * @return 사용자 존재 여부
     */
    public boolean existsById(Long id) {
        try {
            return userRepository.existsById(id);
        } catch (Exception e) {
            throw new RuntimeException("사용자 존재 여부 확인 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 이름으로 사용자를 조회합니다.
     *
     * @param username 사용자 이름
     * @return 사용자 객체
     */
    public User getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("사용자 이름으로 사용자 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 새로운 사용자를 등록합니다. 기본적으로 ROLE_USER 권한을 부여합니다.
     *
     * @param user 등록할 사용자 객체
     * @return 등록 성공 여부
     */
    public boolean registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return false; // 사용자 이름 중복
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                throw new IllegalStateException("ROLE_USER 역할이 시스템에 존재하지 않습니다.");
            }

            user.setRoles(new ArrayList<>());
            user.getRoles().add(userRole);

            userRepository.save(user);
            return true; // 회원가입 성공
        } catch (Exception e) {
            throw new RuntimeException("사용자 등록 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 비밀번호 검증 메소드. 입력된 비밀번호가 저장된 비밀번호와 일치하는지 확인합니다.
     *
     * @param rawPassword 입력된 비밀번호
     * @param encodedPassword 저장된 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
