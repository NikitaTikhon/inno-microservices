package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.exception.ResourceAlreadyExistsException;
import com.innowise.authenticationservice.exception.ResourceNotFoundException;
import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.AuthRequest;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.model.entity.Role;
import com.innowise.authenticationservice.model.entity.User;
import com.innowise.authenticationservice.repository.RoleRepository;
import com.innowise.authenticationservice.repository.UserRepository;
import com.innowise.authenticationservice.service.UserService;
import com.innowise.authenticationservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto save(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            throw new ResourceAlreadyExistsException(ExceptionMessageGenerator.userExists(authRequest.getEmail()));
        }

        User user = User.builder()
                .email(authRequest.getEmail())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .build();

        Role role = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.roleNotFound(RoleEnum.ROLE_USER)));
        user.setRoles(List.of(role));

        userRepository.save(user);

        return UserDto.of(user);
    }

}
