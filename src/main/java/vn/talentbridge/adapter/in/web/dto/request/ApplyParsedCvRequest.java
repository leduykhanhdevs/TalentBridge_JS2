package vn.talentbridge.adapter.in.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyParsedCvRequest {

    private String fullName;
    private String phone;
    private String title;
    private String city;
    private String summary;
    private List<String> skills;
    private List<ParsedExperienceRequest> experiences;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedExperienceRequest {
        private String companyName;
        private String position;
        private String startDate;
        private String endDate;
        private Boolean isCurrent;
        private String description;
    }
}
