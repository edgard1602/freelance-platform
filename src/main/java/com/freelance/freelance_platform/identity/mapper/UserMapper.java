package com.freelance.freelance_platform.identity.mapper;


import com.freelance.freelance_platform.identity.domain.User;
import com.freelance.freelance_platform.identity.dto.UserDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDto toDto(User user);
}