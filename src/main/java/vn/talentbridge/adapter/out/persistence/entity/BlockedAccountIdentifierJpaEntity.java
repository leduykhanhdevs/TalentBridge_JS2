package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_account_identifiers", uniqueConstraints = @UniqueConstraint(
        name = "uk_blocked_account_identifier", columnNames = {"identifier_type", "identifier_hash"}))
@Getter
@Setter
@NoArgsConstructor
public class BlockedAccountIdentifierJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier_type", nullable = false, length = 16)
    private String identifierType;

    @Column(name = "identifier_hash", nullable = false, length = 64)
    private String identifierHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
