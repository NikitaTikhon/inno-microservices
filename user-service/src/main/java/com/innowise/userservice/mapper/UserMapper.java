package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.dto.response.UserWithCardInfoResponse;
import com.innowise.userservice.entity.User;
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
