package vn.talentbridge.adapter.in.web.dto.response;

import vn.talentbridge.core.application.dto.AssistantChatResult;

import java.util.List;

public record AssistantChatResponse(String answer, String source, List<String> references) {
    public static AssistantChatResponse from(AssistantChatResult result) {
        return new AssistantChatResponse(result.answer(), result.source(), result.references());
    }
}
