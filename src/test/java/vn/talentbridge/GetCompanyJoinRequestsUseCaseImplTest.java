package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.GetCompanyJoinRequestsUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCompanyJoinRequestsUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    private GetCompanyJoinRequestsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCompanyJoinRequestsUseCaseImpl(recruiterRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Lấy danh sách yêu cầu gia nhập của công ty thành công")
    void getCompanyJoinRequestsSuccess() {
        Long userId = 10L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);

        Recruiter recruiter = new Recruiter();
        recruiter.setId(1L);
        recruiter.setCompany(company);

        CompanyJoinRequest req = new CompanyJoinRequest();
        req.setId(100L);
        req.setCompany(company);
        req.setStatus(CompanyJoinRequestStatus.PENDING);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyJoinRequestRepository.findByCompanyId(companyId, CompanyJoinRequestStatus.PENDING, 0, 10))
                .thenReturn(List.of(req));

        List<CompanyJoinRequestResult> results = useCase.getCompanyJoinRequests(userId, CompanyJoinRequestStatus.PENDING, 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).id());
    }

    @Test
    @DisplayName("Đếm số lượng yêu cầu gia nhập thành công")
    void countCompanyJoinRequestsSuccess() {
        Long userId = 10L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);

        Recruiter recruiter = new Recruiter();
        recruiter.setId(1L);
        recruiter.setCompany(company);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyJoinRequestRepository.countByCompanyId(companyId, CompanyJoinRequestStatus.PENDING)).thenReturn(5L);

        long count = useCase.countCompanyJoinRequests(userId, CompanyJoinRequestStatus.PENDING);
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Ném ngoại lệ IllegalArgumentException nếu userId null")
    void throwsIllegalArgumentExceptionWhenUserIdNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.getCompanyJoinRequests(null, null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> useCase.countCompanyJoinRequests(null, null));
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException nếu không tìm thấy recruiter")
    void throwsResourceNotFoundWhenRecruiterMissing() {
        when(recruiterRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.getCompanyJoinRequests(999L, null, 0, 10));
        assertThrows(ResourceNotFoundException.class, () -> useCase.countCompanyJoinRequests(999L, null));
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu recruiter chưa có công ty")
    void throwsDomainExceptionWhenRecruiterHasNoCompany() {
        Long userId = 10L;
        Recruiter recruiter = new Recruiter();
        recruiter.setId(1L);
        recruiter.setCompany(null);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));

        assertThrows(DomainException.class, () -> useCase.getCompanyJoinRequests(userId, null, 0, 10));
        assertThrows(DomainException.class, () -> useCase.countCompanyJoinRequests(userId, null));
    }
}
