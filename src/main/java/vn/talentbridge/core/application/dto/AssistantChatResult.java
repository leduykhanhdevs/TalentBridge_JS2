package vn.talentbridge.core.application.dto;

import java.util.List;

public record AssistantChatResult(String answer, String source, List<String> references) {
    public AssistantChatResult {
        references = references == null ? List.of() : List.copyOf(references);
    }
}
