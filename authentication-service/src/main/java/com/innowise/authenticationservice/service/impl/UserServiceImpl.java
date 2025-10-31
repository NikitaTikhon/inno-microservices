package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.exception.ResourceAlreadyExistsException;
import com.innowise.authenticationservice.exception.ResourceNotFoundException;
import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.dto.RegistrationRequest;
import com.innowise.authenticationservice.model.dto.UserDto;
import com.innowise.authenticationservice.model.entity.Role;
import com.innowise.authenticationservice.model.entity.User;
import com.innowise.authenticationservice.repository.RoleRepository;
import com.innowise.authenticationservice.repository.UserRepository;
import com.innowise.authenticationservice.service.UserService;
import com.innowise.authenticationservice.service.UserServiceRestClient;
import com.innowise.authenticationservice.util.ExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserServiceRestClient userServiceRestClient;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto save(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new ResourceAlreadyExistsException(ExceptionMessageGenerator.userExists(registrationRequest.getEmail()));
        }
        UserDto userServiceDto = UserDto.builder()
                .name(registrationRequest.getName())
                .surname(registrationRequest.getSurname())
                .birthDate(registrationRequest.getBirthDate())
                .email(registrationRequest.getEmail())
                .build();

        UserDto userDto = userServiceRestClient.save(userServiceDto);

        try {
            Role role = roleRepository.findByName(RoleEnum.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessageGenerator.roleNotFound(RoleEnum.ROLE_USER)));

            User user = User.builder()
                    .id(userDto.getId())
                    .email(userDto.getEmail())
                    .password(passwordEncoder.encode(registrationRequest.getPassword()))
                    .roles(List.of(role))
                    .build();

            userRepository.saveWithExplicitId(user);
            userRepository.saveUserRole(user.getId(), role.getId());

            userDto.setRoles(user.getRoles().stream().map(Role::getName).toList());

            return userDto;
        } catch (Exception e) {
            try {
                userServiceRestClient.deleteById(userDto.getId());
            } catch (Exception rollbackError) {
                log.error("CRITICAL: Rollback failed for user {}",
                        userDto.getId(), rollbackError);
            }
            throw e;
        }
    }

}
