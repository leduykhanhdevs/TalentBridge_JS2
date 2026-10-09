package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssistantChatRequest(
        @NotBlank
        @Size(max = 1000)
        String message,
        @Size(max = 8)
        List<@Valid AssistantConversationTurnRequest> history
) {
}
