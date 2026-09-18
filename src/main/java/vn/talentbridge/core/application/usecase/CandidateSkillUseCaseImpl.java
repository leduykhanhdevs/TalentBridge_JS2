package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AddCandidateSkillCommand;
import vn.talentbridge.core.application.dto.CandidateSkillResult;
import vn.talentbridge.core.application.dto.SkillResult;
import vn.talentbridge.core.application.port.in.CandidateSkillUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.SkillRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.CandidateSkill;
import vn.talentbridge.core.domain.model.Skill;

import java.util.List;

public class CandidateSkillUseCaseImpl implements CandidateSkillUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;
    private final SkillRepositoryPort skillRepository;

    public CandidateSkillUseCaseImpl(CandidateRepositoryPort candidateRepository,
                                  CandidateSkillRepositoryPort candidateSkillRepository,
                                  SkillRepositoryPort skillRepository) {
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public List<CandidateSkillResult> getCandidateSkills(Long userId) {
        Candidate candidate = getCandidateByUserId(userId);
        return candidateSkillRepository.findByCandidateId(candidate.getId()).stream()
                .map(CandidateSkillResult::from)
                .toList();
    }

    @Override
    public CandidateSkillResult addCandidateSkill(Long userId, AddCandidateSkillCommand command) {
        Candidate candidate = getCandidateByUserId(userId);

        Skill skill;
        if (command.skillId() != null) {
            skill = skillRepository.findById(command.skillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng", command.skillId()));
        } else if (command.skillName() != null && !command.skillName().isBlank()) {
            String normalizedName = command.skillName().trim();
            skill = skillRepository.findByName(normalizedName)
                    .orElseGet(() -> skillRepository.save(new Skill(null, normalizedName)));
        } else {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập tên kỹ năng");
        }

        int rating = command.rating() != null ? command.rating() : 3;
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Mức độ đánh giá kỹ năng phải từ 1 đến 5 sao");
        }

        String proficiency = command.proficiencyLevel() != null && !command.proficiencyLevel().isBlank()
                ? command.proficiencyLevel().trim()
                : getDefaultProficiencyForRating(rating);

        Double years = command.yearsOfExperience() != null ? command.yearsOfExperience() : 1.0;

        CandidateSkill candidateSkill = candidateSkillRepository
                .findByCandidateIdAndSkillId(candidate.getId(), skill.getId())
                .orElse(new CandidateSkill(null, candidate.getId(), skill.getId(), skill.getName(), proficiency, rating, years));

        candidateSkill.setSkillName(skill.getName());
        candidateSkill.setRating(rating);
        candidateSkill.setProficiencyLevel(proficiency);
        candidateSkill.setYearsOfExperience(years);

        CandidateSkill saved = candidateSkillRepository.save(candidateSkill);
        return CandidateSkillResult.from(saved);
    }

    @Override
    public void deleteCandidateSkill(Long userId, Long candidateSkillId) {
        Candidate candidate = getCandidateByUserId(userId);

        CandidateSkill cs = candidateSkillRepository.findById(candidateSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng ứng viên", candidateSkillId));

        if (!cs.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa kỹ năng này");
        }

        candidateSkillRepository.deleteById(candidateSkillId);
    }

    @Override
    public List<SkillResult> getAllAvailableSkills() {
        return skillRepository.findAll().stream()
                .map(SkillResult::from)
                .toList();
    }

    private Candidate getCandidateByUserId(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên", userId));
    }

    private String getDefaultProficiencyForRating(int rating) {
        return switch (rating) {
            case 1 -> "BEGINNER";
            case 2 -> "ELEMENTARY";
            case 3 -> "INTERMEDIATE";
            case 4 -> "ADVANCED";
            case 5 -> "EXPERT";
            default -> "INTERMEDIATE";
        };
    }
}
