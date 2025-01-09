package com.tangeedad.myhome.repository;

import com.tangeedad.myhome.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findByUsernameContaining(String username);

    @Query("select u from User u where u.username like %?1%")
    List<User> findByUsernameQuery(String username);
}
