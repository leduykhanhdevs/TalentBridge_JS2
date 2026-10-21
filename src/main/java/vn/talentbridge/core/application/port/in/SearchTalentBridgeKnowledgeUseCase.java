package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.TalentBridgeKnowledgeEntry;

import java.util.List;

public interface SearchTalentBridgeKnowledgeUseCase {
    List<TalentBridgeKnowledgeEntry> search(String query, int limit);
}
