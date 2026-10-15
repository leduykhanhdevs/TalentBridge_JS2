package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.TalentBridgeKnowledgeEntry;
import vn.talentbridge.core.application.port.in.SearchTalentBridgeKnowledgeUseCase;
import vn.talentbridge.core.application.port.out.TalentBridgeKnowledgePort;

import java.util.List;

public class SearchTalentBridgeKnowledgeUseCaseImpl implements SearchTalentBridgeKnowledgeUseCase {
    private static final int MAX_QUERY_LENGTH = 500;
    private static final int MAX_RESULTS = 5;

    private final TalentBridgeKnowledgePort knowledgePort;

    public SearchTalentBridgeKnowledgeUseCaseImpl(TalentBridgeKnowledgePort knowledgePort) {
        this.knowledgePort = knowledgePort;
    }

    @Override
    public List<TalentBridgeKnowledgeEntry> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung cần tìm trong hướng dẫn TalentBridge.");
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Nội dung tìm kiếm không được vượt quá 500 ký tự.");
        }
        if (limit < 1 || limit > MAX_RESULTS) {
            throw new IllegalArgumentException("Số kết quả phải từ 1 đến 5.");
        }
        return knowledgePort.search(query.trim(), limit);
    }
}
