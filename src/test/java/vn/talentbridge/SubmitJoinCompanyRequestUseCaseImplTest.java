package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.SubmitJoinCompanyRequestCommand;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.SubmitJoinCompanyRequestUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitJoinCompanyRequestUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CompanyRepositoryPort companyRepository;

    @Mock
    private CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    private SubmitJoinCompanyRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitJoinCompanyRequestUseCaseImpl(
                recruiterRepository,
                companyRepository,
                companyJoinRequestRepository
        );
    }

    @Test
    @DisplayName("Nộp đơn xin gia nhập thành công với vị trí và lời nhắn cụ thể")
    void submitJoinRequestSuccess() {
        Long userId = 100L;
        Long companyId = 200L;

        User user = new User();
        user.setId(userId);
        user.setFullName("Nguyen Van A");

        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setUser(user);
        recruiter.setCompany(null);
        recruiter.setPosition("Junior Recruiter");

        Company company = new Company();
        company.setId(companyId);
        company.setName("Tech Corp");
        company.setStatus(CompanyStatus.APPROVED);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyJoinRequestRepository.existsByUserIdAndStatus(userId, CompanyJoinRequestStatus.PENDING)).thenReturn(false);

        LocalDateTime now = LocalDateTime.now();
        when(companyJoinRequestRepository.save(any(CompanyJoinRequest.class))).thenAnswer(inv -> {
            CompanyJoinRequest req = inv.getArgument(0);
            req.setId(500L);
            req.setCreatedAt(now);
            req.setUpdatedAt(now);
            return req;
        });

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand(
                "Senior Talent Acquisition",
                "Toi muon gia nhap doi ngu tuyen dung"
        );

        CompanyJoinRequestResult result = useCase.submitJoinRequest(userId, companyId, command);

        assertNotNull(result);
        assertEquals(500L, result.id());
        assertEquals("Tech Corp", result.companyName());
        assertEquals("Senior Talent Acquisition", result.position());
        assertEquals("Toi muon gia nhap doi ngu tuyen dung", result.message());
        assertEquals("PENDING", result.status());

        ArgumentCaptor<CompanyJoinRequest> captor = ArgumentCaptor.forClass(CompanyJoinRequest.class);
        verify(companyJoinRequestRepository).save(captor.capture());
        CompanyJoinRequest saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(company, saved.getCompany());
        assertEquals("Senior Talent Acquisition", saved.getPosition());
        assertEquals("Toi muon gia nhap doi ngu tuyen dung", saved.getMessage());
        assertEquals(CompanyJoinRequestStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Nộp đơn xin gia nhập lấy chức danh mặc định từ hồ sơ HR nếu command position để trống")
    void submitJoinRequestFallsBackToRecruiterPositionWhenBlank() {
        Long userId = 100L;
        Long companyId = 200L;

        User user = new User();
        user.setId(userId);

        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setUser(user);
        recruiter.setCompany(null);
        recruiter.setPosition("Talent Specialist");

        Company company = new Company();
        company.setId(companyId);
        company.setName("Tech Corp");
        company.setStatus(CompanyStatus.APPROVED);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyJoinRequestRepository.existsByUserIdAndStatus(userId, CompanyJoinRequestStatus.PENDING)).thenReturn(false);

        when(companyJoinRequestRepository.save(any(CompanyJoinRequest.class))).thenAnswer(inv -> {
            CompanyJoinRequest req = inv.getArgument(0);
            req.setId(501L);
            return req;
        });

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("   ", null);

        CompanyJoinRequestResult result = useCase.submitJoinRequest(userId, companyId, command);

        assertNotNull(result);
        assertEquals("Talent Specialist", result.position());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Ném ngoại lệ IllegalArgumentException nếu tham số đầu vào bị null")
    void throwsIllegalArgumentExceptionWhenArgumentsNull() {
        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");

        assertThrows(IllegalArgumentException.class, () -> useCase.submitJoinRequest(null, 1L, command));
        assertThrows(IllegalArgumentException.class, () -> useCase.submitJoinRequest(1L, null, command));
        assertThrows(IllegalArgumentException.class, () -> useCase.submitJoinRequest(1L, 1L, null));

        verifyNoInteractions(recruiterRepository, companyRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException nếu không tìm thấy hồ sơ Recruiter")
    void throwsResourceNotFoundWhenRecruiterMissing() {
        when(recruiterRepository.findByUserId(999L)).thenReturn(Optional.empty());

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");
        assertThrows(ResourceNotFoundException.class, () -> useCase.submitJoinRequest(999L, 1L, command));

        verifyNoInteractions(companyRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu Recruiter đã thuộc về một công ty khác")
    void throwsDomainExceptionWhenRecruiterAlreadyHasCompany() {
        Long userId = 100L;
        Company existingCompany = new Company();
        existingCompany.setId(99L);
        existingCompany.setName("Old Company");

        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setCompany(existingCompany);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.submitJoinRequest(userId, 200L, command));

        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("đã thuộc về một công ty"));
        verifyNoInteractions(companyRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException nếu không tìm thấy công ty theo ID")
    void throwsResourceNotFoundWhenCompanyMissing() {
        Long userId = 100L;
        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setCompany(null);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");
        assertThrows(ResourceNotFoundException.class, () -> useCase.submitJoinRequest(userId, 999L, command));

        verifyNoInteractions(companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu công ty chưa có trạng thái APPROVED")
    void throwsDomainExceptionWhenCompanyNotApproved() {
        Long userId = 100L;
        Long companyId = 200L;

        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setCompany(null);

        Company company = new Company();
        company.setId(companyId);
        company.setStatus(CompanyStatus.PENDING);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.submitJoinRequest(userId, companyId, command));

        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("đã được phê duyệt"));
        verify(companyJoinRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40901) nếu Recruiter đang có yêu cầu PENDING chờ xét duyệt")
    void throwsDomainExceptionWhenPendingRequestExists() {
        Long userId = 100L;
        Long companyId = 200L;

        Recruiter recruiter = new Recruiter();
        recruiter.setId(10L);
        recruiter.setCompany(null);

        Company company = new Company();
        company.setId(companyId);
        company.setStatus(CompanyStatus.APPROVED);

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyJoinRequestRepository.existsByUserIdAndStatus(userId, CompanyJoinRequestStatus.PENDING)).thenReturn(true);

        SubmitJoinCompanyRequestCommand command = new SubmitJoinCompanyRequestCommand("Pos", "Msg");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.submitJoinRequest(userId, companyId, command));

        assertEquals(40901, ex.getCode());
        assertTrue(ex.getMessage().contains("đang chờ xét duyệt"));
        verify(companyJoinRequestRepository, never()).save(any());
    }
}
