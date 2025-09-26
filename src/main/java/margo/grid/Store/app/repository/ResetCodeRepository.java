package margo.grid.store.app.repository;

import margo.grid.store.app.entity.ResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ResetCodeRepository extends JpaRepository<ResetCode, UUID> {
    boolean existsByCodeAndExpiresAtAfter(UUID code, LocalDateTime expiresAtAfter);
}
