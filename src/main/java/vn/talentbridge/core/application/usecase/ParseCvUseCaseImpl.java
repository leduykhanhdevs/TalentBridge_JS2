package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ParsedCvResult;
import vn.talentbridge.core.application.port.in.ParseCvUseCase;
import vn.talentbridge.core.application.port.out.CvParserPort;

public class ParseCvUseCaseImpl implements ParseCvUseCase {

    private final CvParserPort cvParserPort;

    public ParseCvUseCaseImpl(CvParserPort cvParserPort) {
        this.cvParserPort = cvParserPort;
    }

    @Override
    public ParsedCvResult parseCv(byte[] bytes, String fileName) {
        return cvParserPort.parse(bytes, fileName);
    }
}
