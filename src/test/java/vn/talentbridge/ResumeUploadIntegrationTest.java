package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.LoginCommand;
import vn.talentbridge.core.application.port.in.LoginUseCase;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResumeUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RoleJpaRepository roleRepository;

    @Autowired
    private CandidateJpaRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCase;

    private String candidateToken;

    @BeforeEach
    void setUp() {
        RoleJpaEntity candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseGet(() -> roleRepository.save(new RoleJpaEntity(null, "ROLE_CANDIDATE", "Candidate Role")));

        UserJpaEntity testUser = new UserJpaEntity();
        testUser.setEmail("resume_test@talentbridge.vn");
        testUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        testUser.setFullName("Lê Ứng Viên");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(candidateRole));
        testUser = userRepository.save(testUser);

        CandidateJpaEntity testCandidate = new CandidateJpaEntity();
        testCandidate.setUser(testUser);
        testCandidate.setTitle("Senior Java Developer");
        candidateRepository.save(testCandidate);

        candidateToken = loginUseCase.login(
                new LoginCommand("resume_test@talentbridge.vn", "Password123!")
        ).accessToken();
    }

    @Test
    @DisplayName("Tải lên CV PDF và DOCX thành công, thiết lập mặc định và xóa CV")
    void testResumeLifecycle_Upload_SetDefault_Download_Delete() throws Exception {
        // 1. Tải lên CV đầu tiên dạng PDF -> Tự động trở thành CV mặc định
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "NguyenVanA_Resume.pdf",
                "application/pdf",
                "%PDF-1.4 sample resume content for test".getBytes()
        );

        String responseJson = mockMvc.perform(multipart("/api/v1/candidates/resumes/upload")
                        .file(pdfFile)
                        .param("title", "CV Tiếng Việt Chuẩn")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.fileName").value("NguyenVanA_Resume.pdf"))
                .andExpect(jsonPath("$.data.title").value("CV Tiếng Việt Chuẩn"))
                .andExpect(jsonPath("$.data.isDefault").value(true))
                .andReturn().getResponse().getContentAsString();

        // 2. Tải lên CV thứ hai dạng DOCX
        MockMultipartFile docxFile = new MockMultipartFile(
                "file",
                "NguyenVanA_CV_English.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "PK sample docx content for test".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/candidates/resumes/upload")
                        .file(docxFile)
                        .param("title", "CV English Version")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("NguyenVanA_CV_English.docx"))
                .andExpect(jsonPath("$.data.isDefault").value(false));

        // 3. Lấy danh sách CV
        mockMvc.perform(get("/api/v1/candidates/resumes")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // 4. Tìm ID của CV thứ 2 để đổi thành mặc định
        // Trích xuất id của CV thứ nhất từ response
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseJson);
        long firstResumeId = rootNode.path("data").path("id").asLong();

        // Download CV
        mockMvc.perform(get("/api/v1/candidates/resumes/" + firstResumeId + "/download")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("NguyenVanA_Resume.pdf")));

        // 5. Xóa CV thứ nhất
        mockMvc.perform(delete("/api/v1/candidates/resumes/" + firstResumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());

        // Danh sách chỉ còn 1 CV và CV còn lại tự động lên làm mặc định
        mockMvc.perform(get("/api/v1/candidates/resumes")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].isDefault").value(true));
    }

    @Test
    @DisplayName("Từ chối file không đúng định dạng PDF/DOCX hoặc rỗng")
    void testUploadResume_InvalidFile_BadRequest() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "dangerous payload".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/candidates/resumes/upload")
                        .file(exeFile)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Định dạng file không hợp lệ")));
    }
}
