package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.AssistantChatResult;
import vn.talentbridge.core.application.dto.AssistantConversationTurn;

import java.util.List;

public interface TalentBridgeAssistantPort {
    AssistantChatResult answer(String message, List<AssistantConversationTurn> history);
}
