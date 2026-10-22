package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.CandidateAvatarUseCase;
import vn.talentbridge.core.application.port.out.FileStoragePort;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class CandidateAvatarUseCaseImpl implements CandidateAvatarUseCase {

    private static final String FILE_NAME_PATTERN = "^[a-fA-F0-9-]{36}\\.(?:jpg|png)$";
    private final FileStoragePort fileStoragePort;

    public CandidateAvatarUseCaseImpl(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public String upload(byte[] imageBytes) {
        String format = validateImage(imageBytes);
        String extension = format.equals("jpeg") ? ".jpg" : ".png";
        String storedKey = fileStoragePort.storeFile("avatars", "avatar" + extension, imageBytes);
        return storedKey.substring(storedKey.lastIndexOf('/') + 1);
    }

    @Override
    public byte[] load(String fileName) {
        if (fileName == null || !fileName.matches(FILE_NAME_PATTERN)) {
            throw new IllegalArgumentException("Ảnh đại diện không tồn tại.");
        }
        return fileStoragePort.loadFile("avatars/" + fileName);
    }

    private String validateImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Ảnh rỗng.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) throw new IllegalArgumentException("Không đọc được ảnh.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Chỉ hỗ trợ ảnh PNG hoặc JPEG.");

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png") && !format.equals("jpeg")) {
                    throw new IllegalArgumentException("Chỉ hỗ trợ ảnh PNG hoặc JPEG.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096 || (long) width * height > 8_000_000L) {
                    throw new IllegalArgumentException("Kích thước ảnh vượt quá giới hạn cho phép.");
                }
                if (reader.read(0) == null) throw new IllegalArgumentException("Ảnh bị lỗi, vui lòng chọn file khác.");
                return format;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Ảnh bị lỗi, vui lòng chọn file khác.", exception);
        }
    }
}
