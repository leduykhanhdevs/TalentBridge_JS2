package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auth_sessions",
        indexes = {
                @Index(
                        name = "idx_auth_sessions_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_auth_sessions_expires_at",
                        columnList = "expires_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_sessions_session_id",
                        columnNames = "session_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSessionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "session_id",
            nullable = false,
            length = 36
    )
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserJpaEntity user;

    @Column(
            name = "refresh_token_hash",
            nullable = false,
            length = 255
    )
    private String refreshTokenHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}