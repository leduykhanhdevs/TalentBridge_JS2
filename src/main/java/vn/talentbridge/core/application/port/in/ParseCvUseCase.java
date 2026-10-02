package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ParsedCvResult;

public interface ParseCvUseCase {
    ParsedCvResult parseCv(byte[] bytes, String fileName);
}
