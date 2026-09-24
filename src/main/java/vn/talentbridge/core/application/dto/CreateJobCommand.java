package vn.talentbridge.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateJobCommand(
        String title,
        String description,
        String requirements,
        String benefits,
        String location,
        String city,
        String address,
        String jobType,
        String experienceLevel,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        Boolean isNegotiable,
        LocalDate deadline,
        List<String> skills
) {
}
