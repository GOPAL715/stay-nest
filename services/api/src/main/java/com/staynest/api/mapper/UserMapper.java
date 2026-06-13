package com.staynest.api.mapper;

import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "emailVerified", source = "emailVerified")
    UserResponse toUserResponse(User user);
}
