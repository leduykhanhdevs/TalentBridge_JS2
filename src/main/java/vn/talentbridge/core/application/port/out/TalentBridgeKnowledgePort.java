package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.TalentBridgeKnowledgeEntry;

import java.util.List;

public interface TalentBridgeKnowledgePort {
    List<TalentBridgeKnowledgeEntry> search(String query, int limit);
}
