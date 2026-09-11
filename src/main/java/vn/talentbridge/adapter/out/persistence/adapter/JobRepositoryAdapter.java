package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
public class JobRepositoryAdapter implements JobRepositoryPort {
    private final JobJpaRepository jobJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;

    public JobRepositoryAdapter(JobJpaRepository jobJpaRepository, CompanyJpaRepository companyJpaRepository) {
        this.jobJpaRepository = jobJpaRepository;
        this.companyJpaRepository = companyJpaRepository;
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
        entity.setJobType(domainJob.getJobType());
        entity.setExperienceLevel(domainJob.getExperienceLevel());
        entity.setMinSalary(domainJob.getMinSalary());
        entity.setMaxSalary(domainJob.getMaxSalary());
        entity.setDeadline(domainJob.getDeadline());
        entity.setStatus(domainJob.getStatus());
        entity.setRecruiterUserId(domainJob.getRecruiterUserId());

        JobJpaEntity saved = jobJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findAll(int page, int size, JobStatus status) {
        Page<JobJpaEntity> resultPage;
        if (status != null) {
            resultPage = jobJpaRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            resultPage = jobJpaRepository.findAll(PageRequest.of(page, size));
        }
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
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

    private Job toDomain(JobJpaEntity entity) {
        Long companyId = entity.getCompany() != null ? entity.getCompany().getId() : null;
        String companyName = entity.getCompany() != null ? entity.getCompany().getName() : null;

        return new Job(
                entity.getId(),
                companyId,
                companyName,
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequirements(),
                entity.getBenefits(),
                entity.getLocation(),
                entity.getJobType(),
                entity.getExperienceLevel(),
                entity.getMinSalary(),
                entity.getMaxSalary(),
                entity.getDeadline(),
                entity.getStatus(),
                entity.getRecruiterUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}