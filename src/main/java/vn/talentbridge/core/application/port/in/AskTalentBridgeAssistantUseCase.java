package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AssistantChatResult;
import vn.talentbridge.core.application.dto.AssistantConversationTurn;

import java.util.List;

public interface AskTalentBridgeAssistantUseCase {
    AssistantChatResult ask(String message, List<AssistantConversationTurn> history);
}
