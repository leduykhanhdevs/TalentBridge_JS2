package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.web.dto.request.AssistantChatRequest;
import vn.talentbridge.adapter.in.web.dto.response.AssistantChatResponse;
import vn.talentbridge.common.ApiResponse;
import vn.talentbridge.core.application.dto.AssistantConversationTurn;
import vn.talentbridge.core.application.port.in.AskTalentBridgeAssistantUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AssistantController {
    private final AskTalentBridgeAssistantUseCase assistantUseCase;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER', 'ADMIN')")
    @Operation(summary = "Hỏi trợ lý trong phạm vi TalentBridge")
    public ResponseEntity<ApiResponse<AssistantChatResponse>> ask(
            @Valid @RequestBody AssistantChatRequest request) {
        List<AssistantConversationTurn> history = request.history() == null
                ? List.of()
                : request.history().stream()
                        .map(turn -> new AssistantConversationTurn(turn.role(), turn.content()))
                        .toList();
        AssistantChatResponse response = AssistantChatResponse.from(
                assistantUseCase.ask(request.message(), history));
        return ResponseEntity.ok(ApiResponse.success("Trợ lý TalentBridge đã phản hồi", response));
    }
}
