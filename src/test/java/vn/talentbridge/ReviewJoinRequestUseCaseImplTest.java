package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.ReviewJoinRequestCommand;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.ReviewJoinRequestUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewJoinRequestUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    private ReviewJoinRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReviewJoinRequestUseCaseImpl(recruiterRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("HR phê duyệt (ACCEPTED) yêu cầu gia nhập: gán công ty và chức danh cho ứng viên")
    void reviewJoinRequestAcceptSuccess() {
        Long reviewerUserId = 10L;
        Long applicantUserId = 20L;
        Long requestId = 100L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);
        company.setName("TalentBridge Tech");

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(company);

        User applicantUser = new User();
        applicantUser.setId(applicantUserId);

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setId(requestId);
        joinRequest.setUser(applicantUser);
        joinRequest.setCompany(company);
        joinRequest.setPosition("Senior HR Specialist");
        joinRequest.setStatus(CompanyJoinRequestStatus.PENDING);

        Recruiter applicant = new Recruiter();
        applicant.setId(2L);
        applicant.setUser(applicantUser);
        applicant.setCompany(null);
        applicant.setPosition("Junior Recruiter");

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(companyJoinRequestRepository.save(any(CompanyJoinRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(recruiterRepository.findByUserId(applicantUserId)).thenReturn(Optional.of(applicant));

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(
                CompanyJoinRequestStatus.ACCEPTED,
                "Chào mừng gia nhập TalentBridge Tech"
        );

        CompanyJoinRequestResult result = useCase.reviewJoinRequest(reviewerUserId, requestId, command);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.status());
        assertEquals("Chào mừng gia nhập TalentBridge Tech", result.reason());

        // Verify applicant was updated with company and new position
        ArgumentCaptor<Recruiter> recruiterCaptor = ArgumentCaptor.forClass(Recruiter.class);
        verify(recruiterRepository).save(recruiterCaptor.capture());
        Recruiter savedApplicant = recruiterCaptor.getValue();
        assertEquals(company, savedApplicant.getCompany());
        assertEquals("Senior HR Specialist", savedApplicant.getPosition());
    }

    @Test
    @DisplayName("HR từ chối (REJECTED) yêu cầu gia nhập: lưu lý do từ chối và không thay đổi công ty của ứng viên")
    void reviewJoinRequestRejectSuccess() {
        Long reviewerUserId = 10L;
        Long applicantUserId = 20L;
        Long requestId = 100L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);
        company.setName("TalentBridge Tech");

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(company);

        User applicantUser = new User();
        applicantUser.setId(applicantUserId);

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setId(requestId);
        joinRequest.setUser(applicantUser);
        joinRequest.setCompany(company);
        joinRequest.setStatus(CompanyJoinRequestStatus.PENDING);

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(companyJoinRequestRepository.save(any(CompanyJoinRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(
                CompanyJoinRequestStatus.REJECTED,
                "Chưa đủ tiêu chuẩn chuyên môn"
        );

        CompanyJoinRequestResult result = useCase.reviewJoinRequest(reviewerUserId, requestId, command);

        assertNotNull(result);
        assertEquals("REJECTED", result.status());
        assertEquals("Chưa đủ tiêu chuẩn chuyên môn", result.reason());

        // Applicant recruiter profile should not be saved or modified
        verify(recruiterRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ngoại lệ IllegalArgumentException nếu tham số đầu vào bị null")
    void throwsIllegalArgumentExceptionWhenArgumentsNull() {
        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");

        assertThrows(IllegalArgumentException.class, () -> useCase.reviewJoinRequest(null, 1L, command));
        assertThrows(IllegalArgumentException.class, () -> useCase.reviewJoinRequest(1L, null, command));
        assertThrows(IllegalArgumentException.class, () -> useCase.reviewJoinRequest(1L, 1L, null));

        verifyNoInteractions(recruiterRepository, companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException nếu không tìm thấy hồ sơ người duyệt")
    void throwsResourceNotFoundWhenReviewerMissing() {
        when(recruiterRepository.findByUserId(999L)).thenReturn(Optional.empty());

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");
        assertThrows(ResourceNotFoundException.class, () -> useCase.reviewJoinRequest(999L, 1L, command));

        verifyNoInteractions(companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu người duyệt chưa thuộc công ty nào")
    void throwsDomainExceptionWhenReviewerHasNoCompany() {
        Long reviewerUserId = 10L;
        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(null);

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.reviewJoinRequest(reviewerUserId, 1L, command));

        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("chưa thuộc công ty"));
        verifyNoInteractions(companyJoinRequestRepository);
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException nếu không tìm thấy yêu cầu gia nhập theo requestId")
    void throwsResourceNotFoundWhenJoinRequestMissing() {
        Long reviewerUserId = 10L;
        Company company = new Company();
        company.setId(50L);

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(company);

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(999L)).thenReturn(Optional.empty());

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");
        assertThrows(ResourceNotFoundException.class, () -> useCase.reviewJoinRequest(reviewerUserId, 999L, command));
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40301) nếu người duyệt thuộc công ty khác với công ty của yêu cầu")
    void throwsDomainExceptionWhenReviewerFromDifferentCompany() {
        Long reviewerUserId = 10L;
        Long requestId = 100L;

        Company reviewerCompany = new Company();
        reviewerCompany.setId(50L);

        Company otherCompany = new Company();
        otherCompany.setId(99L);

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(reviewerCompany);

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setId(requestId);
        joinRequest.setCompany(otherCompany);

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.reviewJoinRequest(reviewerUserId, requestId, command));

        assertEquals(40301, ex.getCode());
        assertTrue(ex.getMessage().contains("không có quyền"));
        verify(companyJoinRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu yêu cầu gia nhập không ở trạng thái PENDING")
    void throwsDomainExceptionWhenRequestNotPending() {
        Long reviewerUserId = 10L;
        Long requestId = 100L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(company);

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setId(requestId);
        joinRequest.setCompany(company);
        joinRequest.setStatus(CompanyJoinRequestStatus.ACCEPTED); // Already accepted

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

        ReviewJoinRequestCommand command = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.ACCEPTED, "OK");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.reviewJoinRequest(reviewerUserId, requestId, command));

        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("đã được xử lý"));
        verify(companyJoinRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ngoại lệ DomainException(40001) nếu command status là PENDING hoặc null")
    void throwsDomainExceptionWhenCommandStatusInvalid() {
        Long reviewerUserId = 10L;
        Long requestId = 100L;
        Long companyId = 50L;

        Company company = new Company();
        company.setId(companyId);

        Recruiter reviewer = new Recruiter();
        reviewer.setId(1L);
        reviewer.setCompany(company);

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setId(requestId);
        joinRequest.setCompany(company);
        joinRequest.setStatus(CompanyJoinRequestStatus.PENDING);

        when(recruiterRepository.findByUserId(reviewerUserId)).thenReturn(Optional.of(reviewer));
        when(companyJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

        ReviewJoinRequestCommand commandNull = new ReviewJoinRequestCommand(null, "OK");
        assertThrows(DomainException.class, () -> useCase.reviewJoinRequest(reviewerUserId, requestId, commandNull));

        ReviewJoinRequestCommand commandPending = new ReviewJoinRequestCommand(CompanyJoinRequestStatus.PENDING, "OK");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.reviewJoinRequest(reviewerUserId, requestId, commandPending));

        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("chỉ được là ACCEPTED hoặc REJECTED"));
    }
}
