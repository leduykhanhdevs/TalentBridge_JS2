package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationJpaEntity application;

    @Column(name = "interview_time", nullable = false)
    private LocalDateTime interviewTime;

    @Column(name = "location_type", length = 20, nullable = false)
    @Builder.Default
    private String locationType = "ONLINE";

    @Column(name = "meeting_link_or_address", length = 500, nullable = false)
    private String meetingLinkOrAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
