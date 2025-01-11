package com.tangeedad.myhome.dto;

import com.tangeedad.myhome.entity.Role;
import com.tangeedad.myhome.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private List<String> roles;

    public UserDto() {
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}
