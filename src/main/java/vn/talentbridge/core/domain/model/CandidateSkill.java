package vn.talentbridge.core.domain.model;

public class CandidateSkill {
    private Long id;
    private Long candidateId;
    private Integer skillId;
    private String skillName;
    private String proficiencyLevel;
    private Integer rating;
    private Double yearsOfExperience;

    public CandidateSkill() {
    }

    public CandidateSkill(Long id, Long candidateId, Integer skillId, String skillName,
                          String proficiencyLevel, Integer rating, Double yearsOfExperience) {
        this.id = id;
        this.candidateId = candidateId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : "INTERMEDIATE";
        this.rating = rating != null ? rating : 3;
        this.yearsOfExperience = yearsOfExperience != null ? yearsOfExperience : 1.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
