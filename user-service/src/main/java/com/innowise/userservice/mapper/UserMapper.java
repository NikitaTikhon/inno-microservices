package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.dto.UserWithCardInfoResponse;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * This interface defines the contract for mapping between a User entity and a DTO.
 * It's responsible for converting data from the domain model to the data transfer object
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    User userRequestToUser(UserRequest userRequest);

    void updateUserFromUserRequest(UserRequest userRequest, @MappingTarget User user);

    UserResponse userToUserResponse(User user);

    UserWithCardInfoResponse userToUserWithCardInfoResponse(User user);

    List<UserResponse> usersToUsersResponse(List<User> users);

}
