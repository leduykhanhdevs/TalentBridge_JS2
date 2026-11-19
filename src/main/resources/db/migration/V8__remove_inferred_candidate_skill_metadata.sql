-- A skill name from a CV does not prove proficiency or time spent using it.
ALTER TABLE candidate_skills
    CHANGE COLUMN proficiency_level proficiency_level VARCHAR(30) NULL DEFAULT NULL;

ALTER TABLE candidate_skills
    CHANGE COLUMN rating rating INT NULL DEFAULT NULL;

ALTER TABLE candidate_skills
    CHANGE COLUMN years_of_experience years_of_experience DOUBLE NULL DEFAULT NULL;

UPDATE candidate_skills
SET proficiency_level = NULL,
    rating = NULL,
    years_of_experience = NULL;
