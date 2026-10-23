package vn.talentbridge.core.application.dto;

public record ApplicantResumeDownload(String fileName, String fileType, byte[] content) {
}
