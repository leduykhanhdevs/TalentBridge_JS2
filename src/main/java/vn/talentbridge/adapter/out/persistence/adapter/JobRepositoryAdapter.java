package vn.talentbridge.adapter.out.persistence.adapter;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.SkillJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.SkillJpaRepository;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class JobRepositoryAdapter implements JobRepositoryPort {
    private final JobJpaRepository jobJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;
    private final SkillJpaRepository skillJpaRepository;

    public JobRepositoryAdapter(JobJpaRepository jobJpaRepository,
                                CompanyJpaRepository companyJpaRepository,
                                SkillJpaRepository skillJpaRepository) {
        this.jobJpaRepository = jobJpaRepository;
        this.companyJpaRepository = companyJpaRepository;
        this.skillJpaRepository = skillJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findById(Long id) {
        return jobJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Job save(Job domainJob) {
        JobJpaEntity entity;
        if (domainJob.getId() != null) {
            entity = jobJpaRepository.findById(domainJob.getId())
                    .orElse(new JobJpaEntity());
        } else {
            entity = new JobJpaEntity();
        }

        if (domainJob.getCompanyId() != null) {
            CompanyJpaEntity company = companyJpaRepository.findById(domainJob.getCompanyId())
                    .orElse(null);
            entity.setCompany(company);
        }

        entity.setTitle(domainJob.getTitle());
        entity.setDescription(domainJob.getDescription());
        entity.setRequirements(domainJob.getRequirements());
        entity.setBenefits(domainJob.getBenefits());
        entity.setLocation(domainJob.getLocation());
        entity.setCity(domainJob.getCity());
        entity.setAddress(domainJob.getAddress());
        entity.setJobType(domainJob.getJobType());
        entity.setExperienceLevel(domainJob.getExperienceLevel());
        entity.setMinSalary(domainJob.getMinSalary());
        entity.setMaxSalary(domainJob.getMaxSalary());
        entity.setIsNegotiable(domainJob.getIsNegotiable() != null ? domainJob.getIsNegotiable() : false);
        entity.setDeadline(domainJob.getDeadline());
        entity.setStatus(domainJob.getStatus());
        entity.setRecruiterUserId(domainJob.getRecruiterUserId());

        // Process skills
        if (domainJob.getSkills() != null) {
            Set<SkillJpaEntity> skillEntities = new HashSet<>();
            for (String skillName : domainJob.getSkills()) {
                if (skillName != null && !skillName.isBlank()) {
                    String trimmedName = skillName.trim();
                    SkillJpaEntity skill = skillJpaRepository.findByNameIgnoreCase(trimmedName)
                            .orElseGet(() -> skillJpaRepository.save(SkillJpaEntity.builder().name(trimmedName).build()));
                    skillEntities.add(skill);
                }
            }
            entity.setSkills(skillEntities);
        }

        JobJpaEntity saved = jobJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findAll(int page, int size, JobStatus status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobJpaEntity> resultPage;
        if (status != null) {
            resultPage = jobJpaRepository.findByStatus(status, pageRequest);
        } else {
            resultPage = jobJpaRepository.findAll(pageRequest);
        }
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByRecruiterUserId(Long recruiterUserId, int page, int size, JobStatus status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobJpaEntity> resultPage;
        if (status != null) {
            resultPage = jobJpaRepository.findByRecruiterUserIdAndStatus(recruiterUserId, status, pageRequest);
        } else {
            resultPage = jobJpaRepository.findByRecruiterUserId(recruiterUserId, pageRequest);
        }
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRecruiterUserId(Long recruiterUserId, JobStatus status) {
        if (status != null) {
            return jobJpaRepository.findByRecruiterUserIdAndStatus(recruiterUserId, status, PageRequest.of(0, 1)).getTotalElements();
        }
        return jobJpaRepository.findByRecruiterUserId(recruiterUserId, PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByCompanyId(Long companyId, int page, int size, JobStatus status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobJpaEntity> resultPage;
        if (status != null) {
            resultPage = jobJpaRepository.findByCompanyIdAndStatus(companyId, status, pageRequest);
        } else {
            resultPage = jobJpaRepository.findByCompanyId(companyId, pageRequest);
        }
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCompanyId(Long companyId, JobStatus status) {
        if (status != null) {
            return jobJpaRepository.findByCompanyIdAndStatus(companyId, status, PageRequest.of(0, 1)).getTotalElements();
        }
        return jobJpaRepository.findByCompanyId(companyId, PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> search(String keyword, String location, String jobType, String experienceLevel,
                            BigDecimal minSalary, BigDecimal maxSalary, int page, int size) {
        Specification<JobJpaEntity> spec = buildSearchSpecification(keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobJpaRepository.findAll(spec, pageRequest).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countSearch(String keyword, String location, String jobType, String experienceLevel,
                            BigDecimal minSalary, BigDecimal maxSalary) {
        Specification<JobJpaEntity> spec = buildSearchSpecification(keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        return jobJpaRepository.count(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jobJpaRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(JobStatus status) {
        return jobJpaRepository.countByStatus(status);
    }

    private Specification<JobJpaEntity> buildSearchSpecification(String keyword, String location, String jobType,
                                                                 String experienceLevel, BigDecimal minSalary, BigDecimal maxSalary) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only show ACTIVE jobs to public search
            predicates.add(cb.equal(root.get("status"), JobStatus.ACTIVE));

            if (keyword != null && !keyword.isBlank()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate companyMatch = cb.like(cb.lower(root.join("company", JoinType.LEFT).get("name")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch, companyMatch));
            }

            if (location != null && !location.isBlank()) {
                String locPattern = "%" + location.trim().toLowerCase() + "%";
                Predicate locMatch = cb.like(cb.lower(root.get("location")), locPattern);
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), locPattern);
                predicates.add(cb.or(locMatch, cityMatch));
            }

            if (jobType != null && !jobType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("jobType")), jobType.trim().toLowerCase()));
            }

            if (experienceLevel != null && !experienceLevel.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("experienceLevel")), experienceLevel.trim().toLowerCase()));
            }

            if (minSalary != null) {
                // Job's max salary should be >= candidate's min salary requirement, or isNegotiable is true
                Predicate salaryMatch = cb.greaterThanOrEqualTo(root.get("maxSalary"), minSalary);
                Predicate negotiable = cb.isTrue(root.get("isNegotiable"));
                predicates.add(cb.or(salaryMatch, negotiable));
            }

            if (maxSalary != null) {
                // Job's min salary should be <= candidate's max salary budget, or isNegotiable is true
                Predicate salaryMatch = cb.lessThanOrEqualTo(root.get("minSalary"), maxSalary);
                Predicate negotiable = cb.isTrue(root.get("isNegotiable"));
                predicates.add(cb.or(salaryMatch, negotiable));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Job toDomain(JobJpaEntity entity) {
        Long companyId = entity.getCompany() != null ? entity.getCompany().getId() : null;
        String companyName = entity.getCompany() != null ? entity.getCompany().getName() : null;

        List<String> skillNames = entity.getSkills() != null
                ? entity.getSkills().stream().map(SkillJpaEntity::getName).sorted().toList()
                : Collections.emptyList();

        return new Job(
                entity.getId(),
                companyId,
                companyName,
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequirements(),
                entity.getBenefits(),
                entity.getLocation(),
                entity.getCity(),
                entity.getAddress(),
                entity.getJobType(),
                entity.getExperienceLevel(),
                entity.getMinSalary(),
                entity.getMaxSalary(),
                entity.getIsNegotiable(),
                entity.getDeadline(),
                entity.getStatus(),
                entity.getRecruiterUserId(),
                skillNames,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}