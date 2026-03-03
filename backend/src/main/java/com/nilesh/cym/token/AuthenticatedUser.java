package com.nilesh.cym.token;

import com.nilesh.cym.entity.enums.UserRole;

public record AuthenticatedUser(
        Long userId,
        UserRole role,
        String mobile
) {
}
