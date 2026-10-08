package vn.talentbridge.adapter.out.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationNoteJpaRepository;
import vn.talentbridge.core.application.dto.ApplicantFilterCriteria;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.JobApplicant;

import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional
public class JobApplicationPersistenceAdapter implements JobApplicationRepositoryPort {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationNoteJpaRepository applicationNoteJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicant> findApplicants(Long jobId, ApplicantFilterCriteria criteria) {
        Specification<ApplicationJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("job").get("id"), jobId));

            if (criteria != null) {
                if (criteria.getStage() != null && !criteria.getStage().isBlank()) {
                    predicates.add(cb.equal(
                            cb.upper(root.get("currentStage")),
                            criteria.getStage().trim().toUpperCase(Locale.ROOT)
                    ));
                }

                if (criteria.getMinExperience() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("candidate").get("experienceYears"),
                            criteria.getMinExperience()
                    ));
                }

                if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                    String pattern = "%" + criteria.getKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                    Predicate nameMatch = cb.like(cb.lower(root.get("candidate").get("user").get("fullName")), pattern);
                    Predicate emailMatch = cb.like(cb.lower(root.get("candidate").get("user").get("email")), pattern);
                    Predicate phoneMatch = cb.like(cb.lower(root.get("candidate").get("user").get("phoneNumber")), pattern);
                    predicates.add(cb.or(nameMatch, emailMatch, phoneMatch));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = resolveSort(criteria);
        List<ApplicationJpaEntity> entities = applicationJpaRepository.findAll(spec, sort);

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> avgRatings = new HashMap<>();
        Map<Long, Integer> notesCounts = new HashMap<>();
        List<Long> applicationIds = entities.stream().map(ApplicationJpaEntity::getId).toList();
        loadRatingSummaries(applicationIds, avgRatings, notesCounts);

        return entities.stream()
                .map(entity -> toDomain(entity, avgRatings.get(entity.getId()), notesCounts.getOrDefault(entity.getId(), 0)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobApplicant> findApplicantById(Long applicationId) {
        return applicationJpaRepository.findDetailedById(applicationId)
                .map(entity -> {
                    Double avgRating = applicationNoteJpaRepository.findAverageRatingByApplicationId(applicationId);
                    int count = applicationNoteJpaRepository.countByApplicationId(applicationId);
                    return toDomain(entity, avgRating, count);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobApplicant> findApplicantByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return applicationJpaRepository.findDetailedByJobIdAndCandidateId(jobId, candidateId)
                .map(entity -> {
                    Double avgRating = applicationNoteJpaRepository.findAverageRatingByApplicationId(entity.getId());
                    int count = applicationNoteJpaRepository.countByApplicationId(entity.getId());
                    return toDomain(entity, avgRating, count);
                });
    }

    @Override
    public void updateStageAndStatus(Long applicationId, String stage, String status) {
        ApplicationJpaEntity entity = applicationJpaRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));

        if (stage != null && !stage.isBlank()) {
            entity.setCurrentStage(stage.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank()) {
            entity.setStatus(status.trim().toUpperCase(Locale.ROOT));
        }
        applicationJpaRepository.save(entity);
    }

    private Sort resolveSort(ApplicantFilterCriteria criteria) {
        if (criteria == null || criteria.getSortBy() == null || criteria.getSortBy().isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        boolean isAsc = "ASC".equalsIgnoreCase(criteria.getSortDirection());
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = criteria.getSortBy().trim().toLowerCase(Locale.ROOT);

        return switch (sortBy) {
            case "score", "aimatchscore" -> Sort.by(direction, "aiMatchScore");
            case "experience", "experienceyears" -> Sort.by(direction, "candidate.experienceYears");
            case "fullname", "name" -> Sort.by(direction, "candidate.user.fullName");
            case "applieddate", "date", "createdat" -> Sort.by(direction, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private void loadRatingSummaries(
            List<Long> applicationIds,
            Map<Long, Double> avgRatings,
            Map<Long, Integer> notesCounts) {
        List<Object[]> rows = applicationNoteJpaRepository.findRatingSummariesByApplicationIds(applicationIds);
        for (Object[] row : rows) {
            if (row[0] instanceof Long appId) {
                Double avg = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                int count = row[2] != null ? ((Number) row[2]).intValue() : 0;
                avgRatings.put(appId, avg);
                notesCounts.put(appId, count);
            }
        }
    }

    private JobApplicant toDomain(ApplicationJpaEntity entity, Double avgRating, int count) {
        JobApplicant domain = new JobApplicant();
        domain.setId(entity.getId());
        if (entity.getJob() != null) {
            domain.setJobId(entity.getJob().getId());
            domain.setJobTitle(entity.getJob().getTitle());
        }
        if (entity.getCandidate() != null) {
            domain.setCandidateId(entity.getCandidate().getId());
            domain.setCandidateTitle(entity.getCandidate().getTitle());
            domain.setCandidateExperienceYears(entity.getCandidate().getExperienceYears());
            domain.setCandidateCity(entity.getCandidate().getCity());
            if (entity.getCandidate().getUser() != null) {
                domain.setCandidateFullName(entity.getCandidate().getUser().getFullName());
                domain.setCandidateEmail(entity.getCandidate().getUser().getEmail());
                domain.setCandidatePhone(entity.getCandidate().getUser().getPhoneNumber());
                domain.setCandidateAvatarUrl(entity.getCandidate().getUser().getAvatarUrl());
            }
        }
        if (entity.getResume() != null) {
            domain.setResumeId(entity.getResume().getId());
            domain.setResumeUrl(entity.getResume().getFileUrl());
            domain.setResumeFileName(entity.getResume().getFileName());
        }
        domain.setCoverLetter(entity.getCoverLetter());
        domain.setCurrentStage(entity.getCurrentStage());
        domain.setStatus(entity.getStatus());
        domain.setAiMatchScore(entity.getAiMatchScore());
        domain.setAppliedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setAverageRating(avgRating);
        domain.setNotesCount(count);

        return domain;
    }
}
