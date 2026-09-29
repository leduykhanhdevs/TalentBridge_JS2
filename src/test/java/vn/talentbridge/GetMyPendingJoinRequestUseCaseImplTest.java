package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.usecase.GetMyPendingJoinRequestUseCaseImpl;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyPendingJoinRequestUseCaseImplTest {

    @Mock
    private CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    private GetMyPendingJoinRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMyPendingJoinRequestUseCaseImpl(companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Trả về yêu cầu gia nhập PENDING nếu có")
    void returnsPendingJoinRequestWhenExists() {
        Long userId = 10L;

        Company company = new Company();
        company.setId(1L);
        company.setName("TalentBridge Tech");

        User user = new User();
        user.setId(userId);

        CompanyJoinRequest req = new CompanyJoinRequest();
        req.setId(50L);
        req.setUser(user);
        req.setCompany(company);
        req.setStatus(CompanyJoinRequestStatus.PENDING);
        req.setPosition("Technical Recruiter");

        when(companyJoinRequestRepository.findPendingByUserId(userId)).thenReturn(Optional.of(req));

        Optional<CompanyJoinRequestResult> result = useCase.getMyPendingJoinRequest(userId);

        assertTrue(result.isPresent());
        assertEquals(50L, result.get().id());
        assertEquals("TalentBridge Tech", result.get().companyName());
        assertEquals("Technical Recruiter", result.get().position());
        assertEquals("PENDING", result.get().status());
    }

    @Test
    @DisplayName("Trả về empty nếu không có yêu cầu nào")
    void returnsEmptyWhenNoneExists() {
        Long userId = 10L;
        when(companyJoinRequestRepository.findPendingByUserId(userId)).thenReturn(Optional.empty());

        Optional<CompanyJoinRequestResult> result = useCase.getMyPendingJoinRequest(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trả về empty nếu userId null")
    void returnsEmptyWhenUserIdNull() {
        Optional<CompanyJoinRequestResult> result = useCase.getMyPendingJoinRequest(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(companyJoinRequestRepository);
    }
}
