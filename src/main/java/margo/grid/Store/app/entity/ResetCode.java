package margo.grid.store.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class ResetCode {

    @Id
    @UuidGenerator
    private UUID code;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(optional = false, targetEntity = User.class)
    private User user;

    public ResetCode(User user, LocalDateTime expiresAt){
        setUser(user);
        setExpiresAt(expiresAt);
    }
}
