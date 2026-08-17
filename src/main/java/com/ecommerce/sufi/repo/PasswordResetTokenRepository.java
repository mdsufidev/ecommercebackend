package com.ecommerce.sufi.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.sufi.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long>{
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    void deleteByUserId(Long userId);
}
