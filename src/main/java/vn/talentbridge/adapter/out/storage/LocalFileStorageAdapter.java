package vn.talentbridge.adapter.out.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.FileStoragePort;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path basePath;

    public LocalFileStorageAdapter(@Value("${talentbridge.upload.dir:uploads}") String uploadDir) {
        Path configuredPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolvedPath;
        try {
            Files.createDirectories(configuredPath);
            resolvedPath = configuredPath;
            log.info("Khởi tạo thư mục lưu trữ file thành công tại: {}", resolvedPath);
        } catch (Exception e) {
            log.warn("Không thể tạo thư mục lưu trữ tại {}: {}. Chuyển sang thư mục tạm hệ thống.", configuredPath, e.getMessage());
            Path fallbackPath = Paths.get(System.getProperty("java.io.tmpdir"), "talentbridge-uploads").toAbsolutePath().normalize();
            try {
                Files.createDirectories(fallbackPath);
                resolvedPath = fallbackPath;
                log.info("Đã chuyển sang thư mục lưu trữ tạm: {}", resolvedPath);
            } catch (Exception ex) {
                throw new IllegalStateException("Không thể tạo cả thư mục lưu trữ chính và thư mục tạm: " + configuredPath, ex);
            }
        }
        this.basePath = resolvedPath;
    }

    @Override
    public String storeFile(String directory, String originalFileName, byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Nội dung file rỗng.");
        }

        String safeFileName = sanitizeFileName(originalFileName);
        String extension = "";
        int dotIndex = safeFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = safeFileName.substring(dotIndex);
        }

        String uniqueFileName = UUID.randomUUID() + extension;
        Path targetDir = basePath.resolve(directory).normalize();
        validatePathWithinBase(targetDir);

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(uniqueFileName).normalize();
            validatePathWithinBase(targetFile);

            Files.write(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return directory + "/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file: " + originalFileName, e);
        }
    }

    @Override
    public byte[] loadFile(String fileUrl) {
        Path filePath = basePath.resolve(fileUrl).normalize();
        validatePathWithinBase(filePath);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File không tồn tại: " + fileUrl);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file: " + fileUrl, e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        Path filePath = basePath.resolve(fileUrl).normalize();
        validatePathWithinBase(filePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "file";
        return Paths.get(fileName).getFileName().toString();
    }

    private void validatePathWithinBase(Path path) {
        if (!path.normalize().startsWith(basePath)) {
            throw new SecurityException("Phát hiện nguy cơ Path Traversal không hợp lệ: " + path);
        }
    }
}
