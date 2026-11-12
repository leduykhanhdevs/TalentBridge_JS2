package vn.talentbridge.core.application.port.in;

public interface CandidateAvatarUseCase {

    String upload(byte[] imageBytes);

    byte[] load(String fileName);
}
