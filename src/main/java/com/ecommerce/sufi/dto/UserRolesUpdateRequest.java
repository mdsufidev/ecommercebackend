package com.ecommerce.sufi.dto;

import java.util.Set;
import com.ecommerce.sufi.model.RoleName;
import jakarta.validation.constraints.NotEmpty;

public record UserRolesUpdateRequest(@NotEmpty(message = "At least one role is required") Set<RoleName> roles) {
}
