package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AssistantConversationTurnRequest(
        @NotBlank
        @Pattern(regexp = "USER|ASSISTANT")
        String role,
        @NotBlank
        @Size(max = 1000)
        String content
) {
}
