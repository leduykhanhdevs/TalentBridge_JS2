package vn.talentbridge;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.talentbridge.adapter.out.parser.CvParserService;
import vn.talentbridge.core.application.dto.ParsedCvResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CvParserServiceTest {

    private final CvParserService parserService = new CvParserService();

    @Test
    @DisplayName("Bóc tách thông tin từ PDF tạo bởi PDFBox")
    void shouldParsePdfCorrectly() throws IOException {
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Tran Minh Anh");
                cs.newLineAtOffset(0, -20);
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Fullstack Developer");
                cs.newLineAtOffset(0, -20);
                cs.showText("Email: minhanh.tran@gmail.com - Phone: 0987654321");
                cs.newLineAtOffset(0, -30);
                cs.showText("Skills: Java, Spring Boot, React, TypeScript, Docker, SQL");
                cs.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        ParsedCvResult result = parserService.parse(pdfBytes, "CV_TranMinhAnh.pdf");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("minhanh.tran@gmail.com");
        assertThat(result.phone()).isEqualTo("0987654321");
        assertThat(result.fullName()).contains("Tran Minh Anh");
        assertThat(result.title()).contains("Fullstack Developer");
        assertThat(result.skills()).contains("Java", "Spring Boot", "React", "TypeScript", "Docker", "SQL");
    }

    @Test
    @DisplayName("Xử lý file rỗng an toàn không gây crash")
    void shouldHandleEmptyFileSafely() {
        ParsedCvResult result = parserService.parse(new byte[0], "empty.pdf");
        assertThat(result).isNotNull();
        assertThat(result.fullName()).isEmpty();
        assertThat(result.email()).isEmpty();
        assertThat(result.skills()).isEmpty();
    }
}
