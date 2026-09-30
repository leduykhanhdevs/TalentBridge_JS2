package vn.talentbridge.core.application.dto;

public class ApplicantFilterCriteria {
    private String keyword;
    private String stage;
    private Integer minExperience;
    private String sortBy;
    private String sortDirection;

    public ApplicantFilterCriteria() {
    }

    public ApplicantFilterCriteria(String keyword, String stage, Integer minExperience, String sortBy, String sortDirection) {
        this.keyword = keyword;
        this.stage = stage;
        this.minExperience = minExperience;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(Integer minExperience) {
        this.minExperience = minExperience;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
