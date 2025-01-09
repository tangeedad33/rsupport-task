package com.tangeedad.myhome.service;

import com.tangeedad.myhome.entity.Role;
import com.tangeedad.myhome.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * RoleService는 사용자 역할 관리를 위한 서비스 클래스입니다.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * 모든 역할을 가져옵니다.
     *
     * @return 역할 목록
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * 이름으로 역할을 가져옵니다.
     *
     * @param name 역할 이름
     * @return Role 객체
     */
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * 역할을 ID로 조회합니다.
     *
     * @param id 역할 ID
     * @return Role 객체(Optional)
     */
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    /**
     * 새로운 역할을 저장합니다.
     *
     * @param role Role 객체
     * @return 저장된 Role 객체
     */
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * 역할을 삭제합니다.
     *
     * @param id 역할 ID
     */
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
