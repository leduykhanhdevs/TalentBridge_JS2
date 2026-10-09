package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.AssistantChatResult;
import vn.talentbridge.core.application.dto.AssistantConversationTurn;
import vn.talentbridge.core.application.port.in.AskTalentBridgeAssistantUseCase;
import vn.talentbridge.core.application.port.out.TalentBridgeAssistantPort;

import java.util.List;

public class AskTalentBridgeAssistantUseCaseImpl implements AskTalentBridgeAssistantUseCase {
    private static final int MAX_MESSAGE_LENGTH = 1_000;
    private static final int MAX_HISTORY_TURNS = 8;

    private final TalentBridgeAssistantPort assistantPort;

    public AskTalentBridgeAssistantUseCaseImpl(TalentBridgeAssistantPort assistantPort) {
        this.assistantPort = assistantPort;
    }

    @Override
    public AssistantChatResult ask(String message, List<AssistantConversationTurn> history) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập câu hỏi về TalentBridge.");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Câu hỏi không được vượt quá 1.000 ký tự.");
        }
        List<AssistantConversationTurn> safeHistory = history == null ? List.of() : List.copyOf(history);
        if (safeHistory.size() > MAX_HISTORY_TURNS) {
            throw new IllegalArgumentException("Chỉ gửi tối đa 8 lượt hội thoại gần nhất.");
        }
        for (AssistantConversationTurn turn : safeHistory) {
            if (turn == null || turn.role() == null
                    || !("USER".equals(turn.role()) || "ASSISTANT".equals(turn.role()))
                    || turn.content() == null || turn.content().isBlank()
                    || turn.content().length() > MAX_MESSAGE_LENGTH) {
                throw new IllegalArgumentException("Lịch sử hội thoại không hợp lệ.");
            }
        }
        return assistantPort.answer(message.trim(), safeHistory);
    }
}
