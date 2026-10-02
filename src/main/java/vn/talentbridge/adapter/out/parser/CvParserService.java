package vn.talentbridge.adapter.out.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import vn.talentbridge.core.application.dto.ParsedCvResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import vn.talentbridge.core.application.port.out.CvParserPort;

@Slf4j
@Service
public class CvParserService implements CvParserPort {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:(?:\\+84|84|0)[35789]\\d{8})|(?:\\b\\d{3}[-. ]?\\d{3}[-. ]?\\d{4}\\b)"
    );

    private static final List<String> COMMON_SKILLS = List.of(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA",
            "React", "ReactJS", "TypeScript", "JavaScript", "Next.js", "Vue", "Angular",
            "Node.js", "Express", "Python", "Django", "FastAPI", "Go", "Golang",
            "C#", ".NET", "ASP.NET", "C++", "PHP", "Laravel",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "CI/CD", "Git", "GitHub", "GitLab",
            "RESTful API", "GraphQL", "Microservices", "Kafka", "RabbitMQ",
            "HTML", "CSS", "TailwindCSS", "Bootstrap", "Figma", "UI/UX", "Scrum", "Agile",
            "Unit Testing", "JUnit", "Jest"
    );

    private static final List<String> COMMON_TITLES = List.of(
            "Software Engineer", "Backend Developer", "Frontend Developer", "Fullstack Developer",
            "Web Developer", "Mobile Developer", "DevOps Engineer", "Data Engineer", "Data Scientist",
            "QA Engineer", "Tester", "QC Engineer", "Product Manager", "Project Manager", "UI/UX Designer",
            "Lập trình viên Java", "Lập trình viên Frontend", "Lập trình viên Backend", "Lập trình viên Fullstack",
            "Kỹ sư phần mềm", "Chuyên viên phát triển phần mềm"
    );

    public ParsedCvResult parse(byte[] bytes, String fileName) {
        String rawText = extractRawText(bytes, fileName);
        if (rawText == null || rawText.isBlank()) {
            return new ParsedCvResult("", "", "", "", "", "", List.of(), List.of(), "");
        }

        String email = extractEmail(rawText);
        String phone = extractPhone(rawText);
        String fullName = extractFullName(rawText, fileName);
        String title = extractTitle(rawText);
        String city = extractCity(rawText);
        String summary = extractSummary(rawText);
        List<String> skills = extractSkills(rawText);
        List<ParsedCvResult.ParsedExperienceItem> experiences = extractExperiences(rawText);

        return new ParsedCvResult(
                fullName,
                email,
                phone,
                title,
                city,
                summary,
                skills,
                experiences,
                rawText.length() > 2000 ? rawText.substring(0, 2000) : rawText
        );
    }

    public String extractRawText(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) return "";
        String lowerName = (fileName != null ? fileName : "").toLowerCase();

        if (lowerName.endsWith(".docx")) {
            return extractTextFromDocx(bytes);
        }

        // Default try PDF
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (Exception e) {
            log.warn("Lỗi đọc PDF, thử giải mã UTF-8 thông thường: {}", e.getMessage());
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private String extractTextFromDocx(byte[] bytes) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    byte[] xmlBytes = zis.readAllBytes();
                    String xml = new String(xmlBytes, StandardCharsets.UTF_8);
                    // Remove XML tags and unescape
                    String text = xml.replaceAll("<w:p.*?>", "\n")
                            .replaceAll("<[^>]+>", " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&quot;", "\"")
                            .replace("&apos;", "'");
                    return text.replaceAll(" +", " ").trim();
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi đọc DOCX: {}", e.getMessage());
        }
        return "";
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            String p = matcher.group().replaceAll("[\\s-.()]", "");
            if (p.startsWith("84")) p = "0" + p.substring(2);
            return p;
        }
        return "";
    }

    private String extractFullName(String text, String fileName) {
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(lines.length, 12); i++) {
            String line = lines[i].trim();
            if (line.isBlank() || line.length() < 3 || line.length() > 50) continue;

            String lower = line.toLowerCase();
            if (lower.contains("curriculum vitae") || lower.contains("resume") || lower.contains("hồ sơ") ||
                    lower.contains("xin việc") || lower.contains("profile") || lower.contains("cv") ||
                    lower.contains("http") || lower.contains("@") || lower.matches(".*\\d{4}.*")) {
                continue;
            }

            // Word count heuristic: typically 2 to 5 words for a name
            String[] words = line.split("\\s+");
            if (words.length >= 2 && words.length <= 5) {
                return line;
            }
        }

        // Fallback from filename
        if (fileName != null && fileName.contains(".")) {
            String base = fileName.substring(0, fileName.lastIndexOf('.'));
            base = base.replaceAll("(?i)(cv|resume|_|[-])", " ").trim();
            if (!base.isBlank()) return base;
        }
        return "";
    }

    private String extractTitle(String text) {
        for (String title : COMMON_TITLES) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(title) + "\\b");
            if (p.matcher(text).find()) {
                return title;
            }
        }

        String[] lines = text.split("\\r?\\n");
        for (int i = 1; i < Math.min(lines.length, 10); i++) {
            String line = lines[i].trim();
            if (line.length() > 5 && line.length() < 60) {
                String lower = line.toLowerCase();
                if (lower.contains("engineer") || lower.contains("developer") || lower.contains("chuyên viên") || lower.contains("designer")) {
                    return line;
                }
            }
        }
        return "";
    }

    private String extractCity(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("hà nội") || lower.contains("ha noi") || lower.contains("hanoi")) return "Hà Nội";
        if (lower.contains("hồ chí minh") || lower.contains("tp.hcm") || lower.contains("tphcm") || lower.contains("saigon")) return "Hồ Chí Minh";
        if (lower.contains("đà nẵng") || lower.contains("da nang")) return "Đà Nẵng";
        if (lower.contains("cần thơ")) return "Cần Thơ";
        if (lower.contains("hải phòng")) return "Hải Phòng";
        return "";
    }

    private String extractSummary(String text) {
        String[] lines = text.split("\\r?\\n");
        boolean inSummary = false;
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            String lower = trimmed.toLowerCase();

            if (lower.contains("mục tiêu") || lower.contains("giới thiệu") || lower.contains("tóm tắt") ||
                    lower.contains("summary") || lower.contains("about me") || lower.contains("objective")) {
                inSummary = true;
                continue;
            }

            if (inSummary) {
                if (lower.contains("kinh nghiệm") || lower.contains("experience") || lower.contains("kỹ năng") ||
                        lower.contains("skills") || lower.contains("học vấn") || lower.contains("education") ||
                        lower.contains("dự án") || lower.contains("projects")) {
                    break;
                }
                sb.append(trimmed).append(" ");
                if (sb.length() > 500) break;
            }
        }

        return sb.toString().trim();
    }

    private List<String> extractSkills(String text) {
        Set<String> matched = new LinkedHashSet<>();
        String lower = text.toLowerCase();

        for (String skill : COMMON_SKILLS) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(skill) + "\\b");
            if (p.matcher(text).find() || lower.contains(skill.toLowerCase())) {
                matched.add(skill);
            }
        }

        return new ArrayList<>(matched);
    }

    private List<ParsedCvResult.ParsedExperienceItem> extractExperiences(String text) {
        List<ParsedCvResult.ParsedExperienceItem> items = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inExp = false;
        String curCompany = "";
        String curPosition = "";
        String curDates = "";
        StringBuilder curDesc = new StringBuilder();

        Pattern datePattern = Pattern.compile("(?:\\d{2}/)?\\d{4}\\s*(?:-|–|to|đến)\\s*(?:(?:\\d{2}/)?\\d{4}|nay|hiện tại|present)", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            String lower = trimmed.toLowerCase();

            if (lower.contains("kinh nghiệm làm việc") || lower.contains("work experience") || lower.contains("kinh nghiệm chuyên môn")) {
                inExp = true;
                continue;
            }

            if (inExp) {
                if (lower.contains("học vấn") || lower.contains("education") || lower.contains("kỹ năng") ||
                        lower.contains("skills") || lower.contains("chứng chỉ") || lower.contains("certifications")) {
                    break;
                }

                Matcher dateMatcher = datePattern.matcher(trimmed);
                if (dateMatcher.find()) {
                    if (!curCompany.isBlank() || !curPosition.isBlank()) {
                        items.add(new ParsedCvResult.ParsedExperienceItem(
                                curCompany.isBlank() ? "Công ty công nghệ" : curCompany,
                                curPosition.isBlank() ? "Nhân viên" : curPosition,
                                parseStartDate(curDates),
                                parseEndDate(curDates),
                                isCurrentDate(curDates),
                                curDesc.toString().trim()
                        ));
                        curDesc = new StringBuilder();
                        curCompany = "";
                        curPosition = "";
                    }
                    curDates = dateMatcher.group();
                    String remainder = trimmed.replace(curDates, "").replaceAll("[-|•,]", "").trim();
                    if (!remainder.isBlank()) {
                        curPosition = remainder;
                    }
                } else if (curPosition.isBlank() && trimmed.length() < 50 && (lower.contains("developer") || lower.contains("engineer") || lower.contains("intern") || lower.contains("lead"))) {
                    curPosition = trimmed;
                } else if (curCompany.isBlank() && trimmed.length() < 60 && !trimmed.startsWith("•") && !trimmed.startsWith("-")) {
                    curCompany = trimmed;
                } else {
                    curDesc.append(trimmed).append("\n");
                }
            }
        }

        if (!curCompany.isBlank() || !curPosition.isBlank()) {
            items.add(new ParsedCvResult.ParsedExperienceItem(
                    curCompany.isBlank() ? "Công ty công nghệ" : curCompany,
                    curPosition.isBlank() ? "Chuyên viên phát triển" : curPosition,
                    parseStartDate(curDates),
                    parseEndDate(curDates),
                    isCurrentDate(curDates),
                    curDesc.toString().trim()
            ));
        }

        return items;
    }

    private String parseStartDate(String dates) {
        if (dates == null || dates.isBlank()) return "2023-01-01";
        Pattern p = Pattern.compile("(\\d{4})");
        Matcher m = p.matcher(dates);
        if (m.find()) {
            return m.group(1) + "-01-01";
        }
        return "2023-01-01";
    }

    private String parseEndDate(String dates) {
        if (dates == null || dates.isBlank() || isCurrentDate(dates)) return null;
        Pattern p = Pattern.compile("(\\d{4})");
        Matcher m = p.matcher(dates);
        String lastYear = "2024";
        while (m.find()) {
            lastYear = m.group(1);
        }
        return lastYear + "-12-31";
    }

    private Boolean isCurrentDate(String dates) {
        if (dates == null) return false;
        String l = dates.toLowerCase();
        return l.contains("nay") || l.contains("hiện tại") || l.contains("present");
    }
}
