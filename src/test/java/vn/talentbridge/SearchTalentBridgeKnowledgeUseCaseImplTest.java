package vn.talentbridge;

import org.junit.jupiter.api.Test;
import vn.talentbridge.core.application.dto.TalentBridgeKnowledgeEntry;
import vn.talentbridge.core.application.port.out.TalentBridgeKnowledgePort;
import vn.talentbridge.core.application.usecase.SearchTalentBridgeKnowledgeUseCaseImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SearchTalentBridgeKnowledgeUseCaseImplTest {
    private final TalentBridgeKnowledgePort knowledgePort = mock(TalentBridgeKnowledgePort.class);
    private final SearchTalentBridgeKnowledgeUseCaseImpl useCase = new SearchTalentBridgeKnowledgeUseCaseImpl(knowledgePort);

    @Test
    void trimsQueryAndDelegatesBoundedSearch() {
        List<TalentBridgeKnowledgeEntry> expected = List.of(
                new TalentBridgeKnowledgeEntry("Tìm việc và ứng tuyển", "Mở mục Việc làm."));
        when(knowledgePort.search("cách ứng tuyển", 3)).thenReturn(expected);

        assertThat(useCase.search("  cách ứng tuyển  ", 3)).isEqualTo(expected);
        verify(knowledgePort).search("cách ứng tuyển", 3);
    }

    @Test
    void rejectsBlankQueriesAndOutOfRangeResultLimits() {
        assertThatThrownBy(() -> useCase.search(" ", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.search("TalentBridge", 6)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(knowledgePort);
    }
}
