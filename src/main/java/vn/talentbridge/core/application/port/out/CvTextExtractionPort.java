package vn.talentbridge.core.application.port.out;

/** Extracts text locally from a stored CV without sending its content to an external AI provider. */
public interface CvTextExtractionPort {
    String extractText(byte[] bytes, String fileName);
}
