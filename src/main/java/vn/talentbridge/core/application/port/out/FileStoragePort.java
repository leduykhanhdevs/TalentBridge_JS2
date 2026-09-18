package vn.talentbridge.core.application.port.out;

public interface FileStoragePort {

    /**
     * Stores a file in the given sub-directory and returns the relative file URL / key.
     */
    String storeFile(String directory, String originalFileName, byte[] content);

    /**
     * Loads the raw byte content of the file from storage.
     */
    byte[] loadFile(String fileUrl);

    /**
     * Deletes the file from storage.
     */
    void deleteFile(String fileUrl);
}
