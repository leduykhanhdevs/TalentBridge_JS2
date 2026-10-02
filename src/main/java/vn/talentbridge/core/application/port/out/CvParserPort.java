package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.ParsedCvResult;

public interface CvParserPort {
    ParsedCvResult parse(byte[] bytes, String fileName);
}
