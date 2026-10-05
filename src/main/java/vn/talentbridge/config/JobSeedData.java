package vn.talentbridge.config;

import java.util.List;

public class JobSeedData {

    public record SeedJobDef(
            String title,
            String companyName,
            String city,
            String location,
            String jobType,
            String experienceLevel,
            int minSalaryMln,
            int maxSalaryMln,
            boolean isNegotiable,
            String description,
            String requirements,
            String benefits,
            List<String> skills
    ) {}

    public static List<SeedJobDef> get100JobDefinitions() {
        return List.of(
            // --- 1. JAVA & SPRING BOOT (20 jobs) ---
            new SeedJobDef(
                "Senior Java Spring Boot Engineer", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, TP. Thủ Đức",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Tham gia phát triển kiến trúc backend microservices cho các dự án FinTech và E-commerce quy mô lớn. Tối ưu hóa hiệu năng cơ sở dữ liệu MySQL và cache Redis.",
                "Tối thiểu 4 năm kinh nghiệm với Java và Spring Boot. Thành thạo Spring Data JPA, Spring Security, Hibernate, MySQL, Docker, RESTful API.",
                "Thu nhập 35 - 50 triệu/tháng + thưởng dự án. Bảo hiểm FPT Care cho bản thân và gia đình. Môi trường quốc tế, hỗ trợ thi chứng chỉ AWS.",
                List.of("Java", "Spring Boot", "SQL / MySQL", "Docker", "Microservices")
            ),
            new SeedJobDef(
                "Java Backend Developer (Microservices & Kafka)", "VNG Corporation", "TP. Hồ Chí Minh", "Z06 KCX Tân Thuận, Quận 7",
                "FULL_TIME", "MIDDLE", 25, 40, false,
                "Phát triển các dịch vụ backend phục vụ hàng chục triệu người dùng Zalo và Zing. Xử lý hàng đợi phân tán bằng Apache Kafka.",
                "Có từ 2-3 năm kinh nghiệm với Java, Spring Boot, Kafka, Redis. Hiểu biết về hệ thống phân tán High Concurrency.",
                "Lương 25 - 40 triệu. Gói bảo hiểm sức khỏe VIP, cơm trưa miễn phí tại VNG Campus, phòng gym, hồ bơi.",
                List.of("Java", "Spring Boot", "Microservices", "Docker")
            ),
            new SeedJobDef(
                "Lead Java Architect - Hệ thống Core Banking", "Viettel Digital", "Hà Nội", "Số 1 Giang Văn Minh, Ba Đình",
                "FULL_TIME", "SENIOR", 50, 75, false,
                "Chịu trách nhiệm thiết kế tổng thể kiến trúc phần mềm, bảo mật giao dịch và mở rộng quy mô hệ thống thanh toán Viettel Money.",
                "Tối thiểu 6 năm kinh nghiệm Java. Có kinh nghiệm làm Solution Architect cho hệ thống ngân hàng, viễn thông hoặc thanh toán.",
                "Lương lên đến 75 triệu. Thưởng cuối năm 4-6 tháng lương. Cơ hội thăng tiến lên Giám đốc khối kỹ thuật.",
                List.of("Java", "Spring Boot", "Microservices", "SQL / MySQL", "AWS")
            ),
            new SeedJobDef(
                "Chuyên viên phát triển Java Spring Cloud", "VNPT IT", "Hà Nội", "Tòa nhà VNPT, Cầu Giấy",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Tham gia xây dựng các nền tảng Chính phủ điện tử và Y tế số trên nền tảng Spring Cloud và Kubernetes.",
                "Tối thiểu 2 năm kinh nghiệm lập trình Java, Spring Cloud, Oracle/PostgreSQL.",
                "Môi trường nhà nước ổn định, chế độ phúc lợi toàn diện, thưởng các dịp lễ tết chu đáo.",
                List.of("Java", "Spring Boot", "PostgreSQL", "RESTful API")
            ),
            new SeedJobDef(
                "Java Backend Engineer - Hệ thống Ví điện tử", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Tòa nhà Phú Mỹ Hưng, Quận 7",
                "FULL_TIME", "SENIOR", 40, 60, false,
                "Tối ưu hóa các API xử lý giao dịch tài chính với độ trễ siêu thấp (< 50ms). Thiết kế luồng dữ liệu chuẩn ACID.",
                "Có từ 4 năm kinh nghiệm với Java Core, Spring Boot, NoSQL, RabbitMQ/Kafka. Tư duy bảo mật tài chính tốt.",
                "Lương 40 - 60 triệu. Thưởng hiệu quả kinh doanh hấp dẫn. Máy tính MacBook Pro M3 Max được cấp mới.",
                List.of("Java", "Spring Boot", "Microservices", "RESTful API")
            ),
            new SeedJobDef(
                "Kỹ sư Backend Java (E-Commerce Platform)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Saigon Centre, Quận 1",
                "FULL_TIME", "MIDDLE", 30, 48, false,
                "Phát triển các module khuyến mãi Flash Sale, giỏ hàng và thanh toán chịu tải hàng triệu request mỗi giây.",
                "3+ năm kinh nghiệm Java backend. Có kinh nghiệm giải quyết nghẽn cổ chai DB và caching nhiều tầng.",
                "Môi trường đa quốc gia năng động, đồng nghiệp tài năng, chính sách cổ phiếu ESOP và ăn xế miễn phí.",
                List.of("Java", "SQL / MySQL", "Docker", "Git / GitHub")
            ),
            new SeedJobDef(
                "Java Software Engineer (Fintech Platform)", "One Mount Group", "Hà Nội", "Times City, Hai Bà Trưng",
                "FULL_TIME", "MIDDLE", 28, 42, false,
                "Xây dựng hạ tầng dịch vụ tài chính cho hệ sinh thái VinID và OneHousing trên nền tảng Google Cloud.",
                "Tối thiểu 3 năm kinh nghiệm với Java Spring Boot. Thành thạo Clean Architecture và Domain-Driven Design.",
                "Lương cạnh tranh 28 - 42 triệu. Gói khám sức khỏe Vinmec cao cấp hàng năm.",
                List.of("Java", "Spring Boot", "Microservices", "PostgreSQL")
            ),
            new SeedJobDef(
                "Lập trình viên Java Senior (Thị trường Nhật Bản)", "RikkeiSoft", "Đà Nẵng", "Tòa nhà Ricco, Hải Châu",
                "FULL_TIME", "SENIOR", 32, 50, false,
                "Phát triển các giải pháp phần mềm doanh nghiệp cho đối tác Nhật Bản. Làm việc trực tiếp với khách hàng Tokyo.",
                "Có từ 4 năm kinh nghiệm Java. Ưu tiên ứng viên có tiếng Nhật N3 hoặc tiếng Anh giao tiếp tốt.",
                "Hỗ trợ onsite Nhật Bản ngắn hạn hoặc dài hạn. Thưởng dự án hấp dẫn, phụ cấp ngoại ngữ.",
                List.of("Java", "Spring Boot", "SQL / MySQL", "RESTful API")
            ),
            new SeedJobDef(
                "Backend Java Developer (Global Projects)", "NashTech Vietnam", "TP. Hồ Chí Minh", "E-Town, Tân Bình",
                "FULL_TIME", "MIDDLE", 26, 38, false,
                "Tham gia vào các dự án chuyển đổi số toàn cầu cho khách hàng tại Anh và châu Âu.",
                "3+ năm kinh nghiệm với Java Spring Boot, RESTful APIs, Unit Testing (JUnit/Mockito).",
                "Môi trường làm việc chuẩn quốc tế, làm việc Hybrid linh hoạt, đào tạo chứng chỉ quốc tế tài trợ 100%.",
                List.of("Java", "Spring Boot", "RESTful API", "Docker")
            ),
            new SeedJobDef(
                "Senior Java Engineer (Enterprise Solution)", "KMS Technology", "TP. Hồ Chí Minh", "Tòa nhà Tản Viên, Tân Bình",
                "FULL_TIME", "SENIOR", 38, 55, false,
                "Xây dựng phần mềm SaaS phục vụ thị trường Mỹ. Áp dụng các mẫu thiết kế tiên tiến và tự động hóa kiểm thử.",
                "4+ năm kinh nghiệm Java, kinh nghiệm viết Clean Code, SOLID và TDD.",
                "Lương thỏa thuận theo năng lực. 18 ngày phép/năm, văn phòng chuẩn Silicon Valley.",
                List.of("Java", "Spring Boot", "Microservices", "Git / GitHub")
            ),
            new SeedJobDef(
                "Java Spring Boot Developer (Phần mềm viễn thông)", "TMA Solutions", "TP. Hồ Chí Minh", "Công viên phần mềm Quang Trung, Q.12",
                "FULL_TIME", "MIDDLE", 20, 32, false,
                "Phát triển các module mạng viễn thông cho đối tác viễn thông Bắc Mỹ.",
                "2+ năm kinh nghiệm Java. Nắm chắc cấu trúc dữ liệu và giải thuật.",
                "Chế độ bảo hiểm toàn diện, nhiều câu lạc bộ thể thao, môi trường thân thiện và ổn định.",
                List.of("Java", "Spring Boot", "SQL / MySQL", "RESTful API")
            ),
            new SeedJobDef(
                "Kỹ sư phần mềm Java (Chuyển đổi số)", "CMC Global", "Hà Nội", "CMC Tower, Cầu Giấy",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Tham gia phát triển các dự án Outsourcing chất lượng cao cho thị trường Hàn Quốc và châu Á.",
                "2-3 năm kinh nghiệm với Spring Boot, Hibernate, Oracle. Tiếng Anh đọc viết tốt.",
                "Lương 22 - 35 triệu. Thưởng hiệu suất dự án theo quý. Lộ trình công danh minh bạch.",
                List.of("Java", "Spring Boot", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Java Developer (Hệ thống thanh toán ZaloPay)", "VNG Corporation", "TP. Hồ Chí Minh", "Z06 KCX Tân Thuận, Quận 7",
                "FULL_TIME", "MIDDLE", 28, 45, false,
                "Trực tiếp xây dựng cổng thanh toán QR Code và kết nối với các ngân hàng lớn tại Việt Nam.",
                "3 năm kinh nghiệm Java, Spring Boot. Hiểu biết về bảo mật thanh toán PCI-DSS là lợi thế lớn.",
                "Đãi ngộ hàng đầu thị trường, văn phòng mở hiện đại, xe đưa đón nhân viên.",
                List.of("Java", "Spring Boot", "SQL / MySQL", "Microservices")
            ),
            new SeedJobDef(
                "Senior Backend Developer (NextTech Ecosystem)", "NextTech Group", "Hà Nội", "VTC Online, Hai Bà Trưng",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Xây dựng hạ tầng thanh toán số Ngân Lượng và nền tảng hậu cần logistics Boxme.",
                "4+ năm kinh nghiệm Java/Spring Boot hoặc Golang. Tư duy sản phẩm và giải quyết vấn đề tốt.",
                "Lương thưởng xứng đáng. Được tham gia trực tiếp vào chiến lược phát triển sản phẩm công nghệ.",
                List.of("Java", "Spring Boot", "Microservices", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Java Web Developer (Middle)", "FPT Software", "Đà Nẵng", "FPT Complex, Ngũ Hành Sơn",
                "FULL_TIME", "MIDDLE", 20, 30, false,
                "Phát triển ứng dụng web doanh nghiệp cho khách hàng Singapore. Phối hợp với team frontend và QA.",
                "Tối thiểu 2 năm kinh nghiệm Java Spring Boot, MySQL. Có khả năng đọc hiểu tài liệu tiếng Anh.",
                "Làm việc tại tòa nhà FPT Complex xanh mát, nhiều tiện ích sân bóng, gym.",
                List.of("Java", "Spring Boot", "RESTful API", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Fresher Java Developer (Đào tạo có lương)", "FPT Software", "Hà Nội", "FPT Tower, Cầu Giấy",
                "FULL_TIME", "FRESHER", 10, 15, false,
                "Dành cho sinh viên mới tốt nghiệp ngành CNTT. Tham gia chương trình đào tạo chuẩn quốc tế và nhận dự án thật sau 2 tháng.",
                "Nắm vững Java Core, OOP, SQL cơ bản. Cam kết làm việc toàn thời gian.",
                "Trợ cấp đào tạo 10 - 15 triệu/tháng. Sau thử việc xét tăng lương theo năng lực.",
                List.of("Java", "SQL / MySQL", "Git / GitHub")
            ),
            new SeedJobDef(
                "Junior Java Spring Boot Developer", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "JUNIOR", 14, 22, false,
                "Đồng hành cùng các Senior Engineer phát triển các tính năng mới cho ứng dụng Zalo.",
                "1-2 năm kinh nghiệm với Java Spring Boot. Ham học hỏi, tư duy logic vững vàng.",
                "Môi trường học hỏi tuyệt vời từ các kỹ sư kỳ cựu. Chế độ đãi ngộ đầy đủ.",
                List.of("Java", "Spring Boot", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Lập trình viên Java 2-3 năm kinh nghiệm", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "MIDDLE", 22, 32, false,
                "Phát triển ứng dụng backend dịch vụ số Viettel.",
                "2+ năm kinh nghiệm với Java, Spring Boot, MySQL/Oracle.",
                "Môi trường năng động, kỷ luật, đãi ngộ cạnh tranh trong ngành.",
                List.of("Java", "Spring Boot", "RESTful API")
            ),
            new SeedJobDef(
                "Thực tập sinh Java Backend (Intern có trợ cấp)", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, Thủ Đức",
                "FULL_TIME", "INTERN", 5, 8, false,
                "Thực tập thực tế trong dự án phần mềm xuất khẩu. Được hướng dẫn kèm cặp 1-1 bởi Mentor giàu kinh nghiệm.",
                "Sinh viên năm cuối ngành CNTT, có điểm GPA khá giỏi. Biết cơ bản về Java và cơ sở dữ liệu.",
                "Hỗ trợ chi phí thực tập 5 - 8 triệu/tháng, dấu mộc thực tập tốt nghiệp, cơ hội lên nhân viên chính thức.",
                List.of("Java", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Java Backend Intern (Chương trình tài năng)", "TMA Solutions", "TP. Hồ Chí Minh", "Quang Trung, Quận 12",
                "FULL_TIME", "INTERN", 4, 7, false,
                "Tham gia khóa thực tập đào tạo lập trình Java chuyên sâu tại Trung tâm đào tạo TMA.",
                "Đam mê lập trình Java, tư duy thuật toán tốt. Tiếng Anh đọc hiểu.",
                "Hỗ trợ dấu mộc báo cáo tốt nghiệp, phụ cấp thực tập hàng tháng, cơ hội ký hợp đồng lao động ngay.",
                List.of("Java", "Git / GitHub")
            ),

            // --- 2. FRONTEND (REACT, TYPESCRIPT, VUE, NEXT.JS) (20 jobs) ---
            new SeedJobDef(
                "Senior Frontend React / TypeScript Developer", "FPT Software", "Hà Nội", "FPT Tower, Cầu Giấy",
                "FULL_TIME", "SENIOR", 32, 48, false,
                "Xây dựng giao diện web portal responsive sử dụng React 19, TypeScript, TailwindCSS và TanStack Query. Đảm bảo Core Web Vitals.",
                "Có từ 4 năm kinh nghiệm React, TypeScript. Nắm sâu State Management, Micro-Frontend, Web Vitals.",
                "Lương 32 - 48 triệu. Cấp MacBook Pro M3. Gói bảo hiểm sức khỏe VIP FPT Care.",
                List.of("React", "TypeScript", "TailwindCSS", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Frontend Engineer (React & TailwindCSS)", "VNG Corporation", "TP. Hồ Chí Minh", "KCX Tân Thuận, Quận 7",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Phát triển giao diện web cho các cổng game và dịch vụ giải trí trực tuyến của VNG.",
                "2+ năm kinh nghiệm với React, TailwindCSS, REST API. Có mắt thẩm mỹ UI/UX tốt.",
                "Lương 22 - 35 triệu, môi trường năng động, cơm trưa và snack miễn phí.",
                List.of("React", "JavaScript", "TailwindCSS", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Web Frontend Developer (Next.js & Performance)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Saigon Centre, Quận 1",
                "FULL_TIME", "SENIOR", 35, 52, false,
                "Tối ưu hóa tốc độ tải trang, SEO và trải nghiệm mua sắm cho hàng chục triệu người dùng trên web Shopee.",
                "3+ năm kinh nghiệm Next.js, Server Components, SSR/SSG, Web Performance Optimization.",
                "Chính sách đãi ngộ hấp dẫn, làm việc với các chuyên gia công nghệ hàng đầu khu vực.",
                List.of("React", "TypeScript", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Senior ReactJS Developer - Cổng thanh toán", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Xây dựng trang quản trị đối tác Merchant Portal và cổng thanh toán Web Checkout của MoMo.",
                "4+ năm kinh nghiệm React, TypeScript, Redux Toolkit/Zustand. Tỉ mỉ về trải nghiệm người dùng.",
                "Lương 35 - 50 triệu. Chế độ chăm sóc sức khỏe toàn diện cho gia đình.",
                List.of("React", "TypeScript", "RESTful API")
            ),
            new SeedJobDef(
                "Kỹ sư Frontend (Vue.js / Nuxt.js)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 24, 36, false,
                "Xây dựng các ứng dụng web phục vụ người tiêu dùng và đại lý phân phối bán lẻ VinShop.",
                "2+ năm kinh nghiệm với Vue.js 3, Pinia, TypeScript, Nuxt.js. Có kinh nghiệm làm responsive design.",
                "Lương 24 - 36 triệu. Được hưởng các chính sách ưu đãi trong hệ sinh thái Vingroup.",
                List.of("JavaScript", "TypeScript", "HTML5 & CSS3", "TailwindCSS")
            ),
            new SeedJobDef(
                "Frontend Developer (TypeScript & TanStack Query)", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "MIDDLE", 22, 34, false,
                "Phát triển trang dashboard phân tích dữ liệu cho khối kinh doanh dịch vụ số.",
                "2+ năm kinh nghiệm lập trình Frontend với React/TypeScript. Hiểu rõ về Data Fetching và Caching.",
                "Thu nhập cạnh tranh, thưởng dự án, lộ trình thăng tiến rõ ràng.",
                List.of("React", "TypeScript", "RESTful API")
            ),
            new SeedJobDef(
                "Angular Enterprise Frontend Engineer", "KMS Technology", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "SENIOR", 32, 45, false,
                "Phát triển các ứng dụng quản lý doanh nghiệp phức tạp sử dụng Angular và RxJS.",
                "3+ năm kinh nghiệm với Angular 15+, RxJS, NgRx, TypeScript. Kinh nghiệm Unit Test Jest/Jasmine.",
                "Môi trường chuyên nghiệp, đánh giá năng lực minh bạch 2 lần/năm.",
                List.of("TypeScript", "JavaScript", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Frontend Web Developer (Zalo Web Platform)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 36, 52, false,
                "Trực tiếp tham gia phát triển Zalo Web và các Mini App trên nền tảng Zalo.",
                "4+ năm kinh nghiệm Frontend. Nắm vững WebSockets, WebRTC, PWA và tối ưu bộ nhớ trình duyệt.",
                "Lương thỏa thuận theo năng lực (36 - 52 triệu). Môi trường công nghệ đỉnh cao.",
                List.of("React", "TypeScript", "JavaScript", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Senior Web UI Developer (Tiki)", "FPT Software", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 30, 45, false,
                "Tối ưu trải nghiệm mua sắm trên web, xây dựng các module tương tác khách hàng thông minh.",
                "3+ năm kinh nghiệm React, CSS-in-JS, Responsive Web Design.",
                "Lương cạnh tranh, văn phòng trẻ trung, đồng nghiệp thân thiện.",
                List.of("React", "JavaScript", "TailwindCSS")
            ),
            new SeedJobDef(
                "Lập trình viên ReactJS (Thị trường Mỹ/Âu)", "NashTech Vietnam", "TP. Hồ Chí Minh", "E-Town, Tân Bình",
                "FULL_TIME", "MIDDLE", 25, 38, false,
                "Phối hợp với team kỹ sư châu Âu xây dựng các nền tảng thương mại trực tuyến.",
                "2-3 năm kinh nghiệm React. Tiếng Anh giao tiếp lưu loát trong công việc.",
                "Làm việc Hybrid (2 ngày tại văn phòng, 3 ngày tại nhà). Tài trợ học tiếng Anh và chứng chỉ.",
                List.of("React", "TypeScript", "HTML5 & CSS3", "Git / GitHub")
            ),
            new SeedJobDef(
                "Junior React Developer", "CMC Global", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "JUNIOR", 14, 22, false,
                "Tham gia dự án phát triển web theo sự hướng dẫn của Technical Lead.",
                "1-2 năm kinh nghiệm ReactJS, HTML5, CSS3, JavaScript. Tinh thần trách nhiệm cao.",
                "Môi trường thân thiện, được đào tạo bài bản về quy trình Agile/Scrum.",
                List.of("React", "JavaScript", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Frontend Engineer (Vue.js)", "TMA Solutions", "TP. Hồ Chí Minh", "Quang Trung, Q.12",
                "FULL_TIME", "MIDDLE", 18, 28, false,
                "Phát triển giao diện web cho hệ thống viễn thông và IoT.",
                "2 năm kinh nghiệm Vue.js hoặc ReactJS. Có khả năng đọc tài liệu tiếng Anh.",
                "Phúc lợi tốt, làm việc ổn định lâu dài, môi trường đoàn kết.",
                List.of("JavaScript", "HTML5 & CSS3", "Git / GitHub")
            ),
            new SeedJobDef(
                "Fresher Frontend Web Developer", "FPT Software", "Đà Nẵng", "Ngũ Hành Sơn, Đà Nẵng",
                "FULL_TIME", "FRESHER", 9, 14, false,
                "Cơ hội cho các bạn trẻ đam mê giao diện web và thiết kế UI/UX.",
                "Biết HTML, CSS, JavaScript, React cơ bản. Có sản phẩm demo cá nhân là lợi thế.",
                "Môi trường FPT Complex hiện đại, lộ trình đào tạo bài bản trở thành Middle Developer.",
                List.of("React", "JavaScript", "HTML5 & CSS3")
            ),
            new SeedJobDef(
                "Thực tập sinh Frontend ReactJS", "KMS Technology", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "INTERN", 5, 8, false,
                "Học việc và tham gia các dự án thực tế tại KMS Technology.",
                "Sinh viên năm cuối chuyên ngành CNTT. Yêu thích Frontend và tư duy thiết kế.",
                "Trợ cấp thực tập, trang bị laptop, cơ hội tuyển dụng chính thức 100% sau kỳ thực tập.",
                List.of("React", "JavaScript")
            ),
            new SeedJobDef(
                "Frontend Web Developer (Remote 100%)", "NextTech Group", "Toàn quốc (Remote)", "Làm việc từ xa",
                "REMOTE", "MIDDLE", 25, 38, false,
                "Làm việc từ xa toàn thời gian phát triển giao diện sàn thương mại điện tử xuyên biên giới.",
                "3+ năm kinh nghiệm React, Next.js, Redux. Khả năng tự quản lý công việc từ xa tốt.",
                "Tự do về địa điểm làm việc, nhận lương đúng hạn, phụ cấp internet hàng tháng.",
                List.of("React", "TypeScript", "TailwindCSS")
            ),

            // --- 3. FULLSTACK DEVELOPER (15 jobs) ---
            new SeedJobDef(
                "Senior Fullstack Engineer (Java + React)", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, Thủ Đức",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Phát triển toàn diện từ backend API đến frontend web portal cho hệ thống y tế thông minh tại Nhật Bản.",
                "4+ năm kinh nghiệm với Java Spring Boot và React/TypeScript. Hiểu rõ kiến trúc Fullstack hiện đại.",
                "Lương 35 - 55 triệu. Thưởng năm hấp dẫn, cơ hội du lịch và công tác Tokyo hàng năm.",
                List.of("Java", "Spring Boot", "React", "TypeScript", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Fullstack Developer (Node.js & React)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 25, 40, false,
                "Xây dựng các web app nội bộ và công cụ quản trị chiến dịch quảng cáo Zalo Ads.",
                "2-3 năm kinh nghiệm với Node.js (NestJS/Express), React, MongoDB hoặc PostgreSQL.",
                "Chế độ đãi ngộ hàng đầu, môi trường làm việc thoải mái sáng tạo.",
                List.of("Node.js", "React", "TypeScript", "PostgreSQL")
            ),
            new SeedJobDef(
                "Fullstack Web Developer (Python + React)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "MIDDLE", 28, 45, false,
                "Phát triển các công cụ phân tích dữ liệu bán hàng và quản lý kho hàng tự động.",
                "3+ năm kinh nghiệm với Python (Django/FastAPI) và React/TypeScript.",
                "Lương thưởng hấp dẫn, làm việc cùng đội ngũ kỹ sư tinh hoa của Shopee.",
                List.of("Python", "React", "SQL / MySQL", "Docker")
            ),
            new SeedJobDef(
                "Kỹ sư Fullstack (Spring Boot + Angular)", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 32, 48, false,
                "Phát triển giải pháp ngân hàng số toàn diện từ phía server đến giao diện người dùng.",
                "3+ năm kinh nghiệm Java Spring Boot và Angular. Khả năng làm việc độc lập tốt.",
                "Môi trường công nghệ quy mô lớn, chế độ đãi ngộ hàng đầu ngành viễn thông.",
                List.of("Java", "Spring Boot", "TypeScript", "Microservices")
            ),
            new SeedJobDef(
                "Senior Fullstack Developer (Go + ReactJS)", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 40, 60, false,
                "Xây dựng các dịch vụ microservices bằng Golang và giao diện quản trị ReactJS cho đối tác MoMo.",
                "4+ năm kinh nghiệm backend (Go hoặc Java) và frontend (React). Tư duy hệ thống phân tán.",
                "Thu nhập 40 - 60 triệu. Đầy đủ quyền lợi phúc lợi tài chính và bảo hiểm cao cấp.",
                List.of("React", "Docker", "Microservices", "RESTful API")
            ),
            new SeedJobDef(
                "Fullstack Developer (Next.js & PostgreSQL)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 26, 38, false,
                "Xây dựng nền tảng công nghệ bất động sản OneHousing tốc độ cao.",
                "2-3 năm kinh nghiệm Next.js, Node.js, Prisma, PostgreSQL. Tối ưu trải nghiệm tìm kiếm nhà.",
                "Chế độ lương cạnh tranh, môi trường Vingroup chuyên nghiệp.",
                List.of("React", "TypeScript", "PostgreSQL", "TailwindCSS")
            ),
            new SeedJobDef(
                "Lập trình viên Fullstack C# .NET & React", "CMC Global", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "MIDDLE", 24, 36, false,
                "Phát triển các ứng dụng quản lý doanh nghiệp ERP cho khách hàng châu Âu.",
                "2+ năm kinh nghiệm C# .NET Core và ReactJS. Thành thạo SQL Server.",
                "Lương 24 - 36 triệu, phụ cấp ngoại ngữ, xét tăng lương định kỳ.",
                List.of("React", "TypeScript", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Senior Fullstack Software Engineer", "NashTech Vietnam", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Tham gia thiết kế và hiện thực hóa các giải pháp phần mềm đa nền tảng cho khách hàng Anh Quốc.",
                "4+ năm kinh nghiệm Fullstack (Node/Java + React/Angular). Tiếng Anh giao tiếp tốt.",
                "Chế độ đãi ngộ quốc tế, làm việc Hybrid, bảo hiểm sức khỏe VIP toàn diện.",
                List.of("Java", "React", "TypeScript", "Docker", "AWS")
            ),
            new SeedJobDef(
                "Junior Fullstack Developer (Java & React)", "TMA Solutions", "TP. Hồ Chí Minh", "Quang Trung, Q.12",
                "FULL_TIME", "JUNIOR", 14, 20, false,
                "Phát triển tính năng mới cho ứng dụng quản trị y tế.",
                "1 năm kinh nghiệm Java và ReactJS. Ham học hỏi, chịu khó nghiên cứu công nghệ.",
                "Môi trường thân thiện, được hỗ trợ nhiệt tình từ các anh chị đi trước.",
                List.of("Java", "React", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Fresher Fullstack Developer", "RikkeiSoft", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "FRESHER", 10, 16, false,
                "Dành cho lập trình viên trẻ muốn phát triển toàn diện cả Backend lẫn Frontend.",
                "Nắm vững một ngôn ngữ backend (Java/Node/PHP) và cơ bản về React hoặc Vue.",
                "Lộ trình phát triển sự nghiệp rõ ràng, thưởng các ngày lễ tết chu đáo.",
                List.of("Java", "React", "Git / GitHub")
            ),

            // --- 4. MOBILE APP DEVELOPER (FLUTTER, REACT NATIVE, IOS, ANDROID) (15 jobs) ---
            new SeedJobDef(
                "Senior React Native Developer (Ví MoMo)", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Chịu trách nhiệm phát triển các luồng tính năng thanh toán, chuyển tiền, Mini App trên app MoMo.",
                "4+ năm kinh nghiệm React Native. Tối ưu hiệu năng 60fps, hiểu sâu Native Modules (iOS/Android).",
                "Lương 35 - 55 triệu. Thưởng cuối năm và các kỳ nghỉ dưỡng team building hàng năm.",
                List.of("React", "TypeScript", "JavaScript")
            ),
            new SeedJobDef(
                "Flutter Mobile Developer (iOS & Android)", "VNG Corporation", "TP. Hồ Chí Minh", "KCX Tân Thuận, Quận 7",
                "FULL_TIME", "MIDDLE", 24, 38, false,
                "Xây dựng ứng dụng di động đa nền tảng cho các sản phẩm game và nội dung số của VNG.",
                "2+ năm kinh nghiệm Flutter/Dart. Thành thạo State Management (Bloc/Provider).",
                "Lương 24 - 38 triệu. Môi trường công nghệ năng động, hiện đại.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Senior iOS Swift Developer (ZaloPay)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Tối ưu hóa và nâng cấp ứng dụng ZaloPay trên nền tảng iOS. Đảm bảo tính bảo mật và trải nghiệm mượt mà.",
                "4+ năm kinh nghiệm Swift, UIKit, SwiftUI. Hiểu sâu về bảo mật ứng dụng iOS và Keychain.",
                "Đãi ngộ cạnh tranh hàng đầu thị trường, bảo hiểm quốc tế cao cấp.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Android Kotlin Senior Developer", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 30, 45, false,
                "Phát triển ứng dụng tài chính số Viettel Money trên nền tảng Android.",
                "3+ năm kinh nghiệm lập trình Android với Kotlin, Jetpack Compose, Coroutines, MVVM.",
                "Môi trường viễn thông quy mô lớn, nhiều thách thức công nghệ.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Mobile App Developer (React Native)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "MIDDLE", 26, 42, false,
                "Phát triển các tính năng mua sắm và livestream trên ứng dụng Shopee.",
                "2-3 năm kinh nghiệm React Native, TypeScript. Có kỹ năng tối ưu memory leak và bundle size.",
                "Môi trường quốc tế, nhiều cơ hội học hỏi từ đội ngũ toàn cầu.",
                List.of("React", "TypeScript")
            ),
            new SeedJobDef(
                "Flutter Developer (Ứng dụng VinID)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Phát triển tính năng mua sắm tích điểm và thanh toán thông minh trên ứng dụng VinID.",
                "2+ năm kinh nghiệm Flutter. Đã từng xuất bản ít nhất 1 ứng dụng lên App Store / Google Play.",
                "Đãi ngộ cạnh tranh, gói bảo hiểm Vinmec, làm việc tại Times City.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Kỹ sư Lập trình Di động (iOS / Android)", "FPT Software", "Đà Nẵng", "Ngũ Hành Sơn, Đà Nẵng",
                "FULL_TIME", "MIDDLE", 20, 32, false,
                "Phát triển các ứng dụng di động cho khách hàng Nhật Bản và châu Âu.",
                "2+ năm kinh nghiệm iOS (Swift) hoặc Android (Kotlin).",
                "Làm việc tại FPT Complex Đà Nẵng, xe đưa đón nội thành, môi trường làm việc thoải mái.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Junior React Native Developer", "FPT Software", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "JUNIOR", 14, 20, false,
                "Tham gia bảo trì và phát triển tính năng mới cho ứng dụng di động của đối tác.",
                "1 năm kinh nghiệm React Native, JavaScript/TypeScript. Đam mê phát triển ứng dụng di động.",
                "Môi trường đào tạo bài bản, cơ hội thăng tiến lên Middle Developer nhanh chóng.",
                List.of("React", "JavaScript")
            ),
            new SeedJobDef(
                "Thực tập sinh Lập trình Di động Flutter", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, Thủ Đức",
                "FULL_TIME", "INTERN", 5, 8, false,
                "Học việc và làm việc trực tiếp trên các dự án app di động thực tế.",
                "Sinh viên năm cuối ngành CNTT, biết lập trình Dart và Flutter cơ bản.",
                "Trợ cấp thực tập, hỗ trợ dấu mộc đồ án, cơ hội trở thành nhân viên chính thức sau 3 tháng.",
                List.of("Git / GitHub")
            ),

            // --- 5. DEVOPS, CLOUD & SRE (10 jobs) ---
            new SeedJobDef(
                "Senior Cloud DevOps Engineer (AWS & Terraform)", "FPT Software", "Đà Nẵng", "FPT Complex, Ngũ Hành Sơn",
                "REMOTE", "SENIOR", 35, 52, false,
                "Xây dựng hạ tầng đám mây AWS tự động hóa (IaC) bằng Terraform, triển khai Kubernetes Cluster quy mô lớn.",
                "4+ năm kinh nghiệm DevOps, chứng chỉ AWS Certified Solutions Architect, thành thạo Docker, K8s, CI/CD.",
                "Làm việc từ xa linh hoạt (Remote 100%), lương lên đến 52 triệu, thưởng quý hấp dẫn.",
                List.of("AWS", "Docker", "Git / GitHub", "Microservices")
            ),
            new SeedJobDef(
                "Kubernetes Platform Engineer (K8s & Docker)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 38, 55, false,
                "Quản lý và vận hành hệ thống container orchestration chạy trên hàng nghìn node máy chủ của VNG.",
                "3+ năm kinh nghiệm sâu về Kubernetes, Linux kernel, CNI, CSI, Prometheus, Grafana.",
                "Môi trường công nghệ hạ tầng quy mô lớn nhất Việt Nam, đãi ngộ VIP.",
                List.of("Docker", "Linux", "Microservices")
            ),
            new SeedJobDef(
                "SRE - Site Reliability Engineer (99.99% Uptime)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 40, 60, false,
                "Đảm bảo độ ổn định và khả năng phục hồi của hệ thống sàn Shopee trong các ngày siêu sale đôi (11.11, 12.12).",
                "4+ năm kinh nghiệm SRE/DevOps. Khả năng debug hệ thống nhanh, viết script tự động hóa bằng Python/Go.",
                "Lương thưởng hàng đầu, làm việc với quy mô traffic hàng triệu người dùng trực tuyến.",
                List.of("Docker", "AWS", "Python", "Linux")
            ),
            new SeedJobDef(
                "DevOps Engineer (CI/CD & Docker)", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 26, 40, false,
                "Tự động hóa luồng build, test và deploy cho hơn 100 microservices của MoMo lên Cloud.",
                "2-3 năm kinh nghiệm với GitLab CI/CD, ArgoCD, Docker, Kubernetes, Linux.",
                "Lương 26 - 40 triệu, máy tính làm việc cấu hình mạnh, chế độ chăm sóc nhân viên tận tâm.",
                List.of("Docker", "Git / GitHub", "Microservices")
            ),
            new SeedJobDef(
                "Cloud Solutions Architect (Viettel Cloud)", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 45, 65, false,
                "Thiết kế kiến trúc hạ tầng Cloud công cộng và Hybrid Cloud cho các khách hàng doanh nghiệp lớn.",
                "5+ năm kinh nghiệm Cloud Architecture. Có chứng chỉ AWS/GCP/Azure Professional.",
                "Lương thỏa thuận (45 - 65 triệu). Thưởng năm vượt chỉ tiêu kinh doanh.",
                List.of("AWS", "Docker", "Microservices")
            ),
            new SeedJobDef(
                "Junior DevOps Engineer", "TMA Solutions", "TP. Hồ Chí Minh", "Quang Trung, Q.12",
                "FULL_TIME", "JUNIOR", 14, 22, false,
                "Hỗ trợ cài đặt máy chủ, cấu hình Docker container và giám sát hệ thống.",
                "1 năm kinh nghiệm hoặc mới tốt nghiệp có chứng chỉ Linux/AWS. Chăm chỉ, cẩn thận.",
                "Môi trường đào tạo thực tế bài bản, cơ hội tiếp cận nhiều công nghệ mới.",
                List.of("Docker", "Git / GitHub")
            ),

            // --- 6. DATA & AI / MACHINE LEARNING (10 jobs) ---
            new SeedJobDef(
                "Senior Data Engineer (Spark, Kafka, Airflow)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Xây dựng đường ống dữ liệu (Data Pipeline) xử lý hàng Terabyte dữ liệu mỗi ngày phục vụ phân tích hành vi mua sắm.",
                "3+ năm kinh nghiệm Data Engineering với Apache Spark, Kafka, Airflow, Hadoop, SQL chuyên sâu.",
                "Lương 35 - 55 triệu. Được làm việc trên cụm Big Data quy mô siêu lớn.",
                List.of("SQL / MySQL", "Python", "PostgreSQL")
            ),
            new SeedJobDef(
                "Data Analyst / BI Specialist (PowerBI, SQL)", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Phân tích dữ liệu người dùng, xây dựng báo cáo Dashboard quản trị và đưa ra gợi ý tối ưu tỷ lệ chuyển đổi.",
                "2+ năm kinh nghiệm Data Analyst. Thành thạo SQL phức tạp, PowerBI, Tableau, Python phân tích.",
                "Lương 22 - 35 triệu. Môi trường dữ liệu lớn phong phú, văn hóa tôn trọng số liệu.",
                List.of("SQL / MySQL", "Python")
            ),
            new SeedJobDef(
                "AI Application Engineer (LLM & GenAI)", "FPT Software", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Nghiên cứu và ứng dụng các mô hình ngôn ngữ lớn (LLM, Gemini, OpenAI) vào các sản phẩm tự động hóa doanh nghiệp.",
                "3+ năm kinh nghiệm Python, LangChain, LlamaIndex, RAG Architecture, Vector Database.",
                "Lương 35 - 55 triệu. Cơ hội đi đầu trong làn sóng công nghệ Generative AI.",
                List.of("Python", "RESTful API", "Docker")
            ),
            new SeedJobDef(
                "Machine Learning Engineer", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 28, 45, false,
                "Xây dựng hệ thống gợi ý nội dung (Recommendation System) cho ứng dụng Zalo và Zing MP3.",
                "2-3 năm kinh nghiệm Machine Learning, Deep Learning, PyTorch, Scikit-learn, MLOps.",
                "Đãi ngộ hấp dẫn, văn phòng làm việc chuẩn quốc tế, trang thiết bị GPU hiện đại.",
                List.of("Python", "Docker")
            ),
            new SeedJobDef(
                "Junior Data Analyst (SQL & Python)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "JUNIOR", 15, 22, false,
                "Thu thập, làm sạch dữ liệu và xây dựng báo cáo phân tích hiệu quả các chiến dịch khuyến mãi.",
                "1 năm kinh nghiệm phân tích dữ liệu, thành thạo viết query SQL, tư duy logic nhạy bén.",
                "Chế độ lương thưởng minh bạch, lộ trình phát triển thành Data Scientist rõ ràng.",
                List.of("SQL / MySQL", "Python")
            ),

            // --- 7. QA & TEST AUTOMATION (10 jobs) ---
            new SeedJobDef(
                "Senior QA Automation Engineer (Playwright & TS)", "KMS Technology", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "SENIOR", 30, 45, false,
                "Thiết kế framework kiểm thử tự động toàn diện từ E2E đến API Testing bằng Playwright và TypeScript.",
                "3+ năm kinh nghiệm Automation Test. Thành thạo TypeScript, Playwright, CI/CD Integration.",
                "Lương 30 - 45 triệu, đánh giá tăng lương 2 lần/năm, văn phòng làm việc năng động.",
                List.of("TypeScript", "JavaScript", "Git / GitHub")
            ),
            new SeedJobDef(
                "QA Automation Lead (Selenium, Java, CI/CD)", "FPT Software", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 32, 48, false,
                "Dẫn dắt đội ngũ QA tự động hóa, xây dựng giải pháp kiểm thử chất lượng cho dự án ngân hàng quốc tế.",
                "4+ năm kinh nghiệm Test Automation với Java, Selenium WebDriver, TestNG, Cucumber, Jenkins.",
                "Lương hấp dẫn, bảo hiểm FPT Care cho gia đình, cơ hội công tác nước ngoài.",
                List.of("Java", "Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "QC Engineer (Manual & API Testing)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 18, 28, false,
                "Kiểm thử chức năng, viết test case, kiểm tra API bằng Postman cho các tính năng mới của ứng dụng Zalo.",
                "2+ năm kinh nghiệm Manual Testing. Cẩn thận, chi tiết, tư duy logic phản biện tốt.",
                "Lương 18 - 28 triệu. Cơm trưa và dịch vụ thể thao giải trí miễn phí tại trụ sở VNG.",
                List.of("RESTful API", "Agile / Scrum")
            ),
            new SeedJobDef(
                "Junior QA / Tester (Web & Mobile)", "TMA Solutions", "TP. Hồ Chí Minh", "Quang Trung, Q.12",
                "FULL_TIME", "JUNIOR", 12, 18, false,
                "Tham gia kiểm thử giao diện và luồng nghiệp vụ ứng dụng web và di động.",
                "1 năm kinh nghiệm kiểm thử phần mềm, hiểu rõ quy trình phát triển phần mềm Agile.",
                "Môi trường đào tạo chuyên nghiệp, nhiều cơ hội học hỏi Test Automation.",
                List.of("Agile / Scrum")
            ),
            new SeedJobDef(
                "Thực tập sinh Kiểm thử phần mềm (QA Intern)", "NashTech Vietnam", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "INTERN", 5, 8, false,
                "Học việc và hỗ trợ đội ngũ QA trong quá trình kiểm thử phần mềm.",
                "Sinh viên năm cuối ngành CNTT, có kiến thức cơ bản về testing và quy trình phần mềm.",
                "Trợ cấp thực tập, cấp laptop làm việc, cơ hội ký hợp đồng chính thức sau 3 tháng.",
                List.of("Agile / Scrum")
            ),

            // --- 8. PRODUCT, DESIGN & BUSINESS ANALYST (10 jobs) ---
            new SeedJobDef(
                "UI/UX Product Designer (Figma)", "FPT Software", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "HYBRID", "MIDDLE", 22, 35, false,
                "Thiết kế trải nghiệm người dùng (UX) và giao diện trực quan (UI) cho các sản phẩm web/mobile. Xây dựng Design System.",
                "2+ năm kinh nghiệm thiết kế UI/UX trên Figma. Có portfolio sản phẩm thực tế ấn tượng.",
                "Lương 22 - 35 triệu. Làm việc Hybrid linh hoạt, cấp MacBook Pro làm việc.",
                List.of("Figma / UI-UX")
            ),
            new SeedJobDef(
                "Senior Product Designer (UI/UX Mobile App)", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 32, 50, false,
                "Trực tiếp thiết kế các trải nghiệm thanh toán và dịch vụ tài chính cho hơn 30 triệu người dùng MoMo.",
                "4+ năm kinh nghiệm thiết kế sản phẩm di động quy mô lớn. Am hiểu sâu sắc về Human Interface Guidelines & Material Design.",
                "Lương 32 - 50 triệu. Thưởng hiệu quả kinh doanh, gói bảo hiểm chăm sóc sức khỏe cao cấp.",
                List.of("Figma / UI-UX")
            ),
            new SeedJobDef(
                "Senior Business Analyst (BA IT & Banking)", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 30, 45, false,
                "Cầu nối giữa khối kinh doanh và đội ngũ kỹ thuật. Phân tích và viết tài liệu yêu cầu nghiệp vụ cho hệ thống Viettel Money.",
                "3+ năm kinh nghiệm làm IT Business Analyst trong ngành tài chính ngân hàng hoặc viễn thông.",
                "Đãi ngộ cạnh tranh hàng đầu thị trường, lộ trình thăng tiến rõ ràng.",
                List.of("Agile / Scrum")
            ),
            new SeedJobDef(
                "Product Manager (E-Commerce Platform)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 45, 70, false,
                "Định hình chiến lược phát triển sản phẩm, tối ưu phễu chuyển đổi mua hàng và dẫn dắt đội ngũ kỹ sư.",
                "4+ năm kinh nghiệm làm Product Manager trong mảng thương mại điện tử hoặc sản phẩm tiêu dùng số.",
                "Thu nhập 45 - 70 triệu. Môi trường quốc tế tài năng và năng động.",
                List.of("Agile / Scrum")
            ),
            new SeedJobDef(
                "Scrum Master / Agile Project Manager", "KMS Technology", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "MIDDLE", 28, 42, false,
                "Tạo điều kiện và thúc đẩy các nhóm phát triển phần mềm vận hành trơn tru theo khung phương pháp Scrum.",
                "2+ năm kinh nghiệm làm Scrum Master. Có chứng chỉ PSM I hoặc CSM là lợi thế lớn.",
                "Lương 28 - 42 triệu. Văn hóa công ty chú trọng con người và sự cân bằng công việc - cuộc sống.",
                List.of("Agile / Scrum")
            ),

            // --- 9. NGÔN NGỮ KHÁC & HỆ THỐNG / BẢO MẬT (25 jobs để đủ 100 jobs) ---
            new SeedJobDef(
                "Golang Backend Engineer (High Concurrency)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 38, 55, false,
                "Xây dựng các core microservices bằng Golang xử lý hàng chục triệu request giao dịch thời gian thực.",
                "3+ năm kinh nghiệm Golang, hiểu sâu về Goroutine, Channel, GC, Memory Management, gRPC.",
                "Lương 38 - 55 triệu. Thưởng cuối năm hấp dẫn, cơ hội tham gia các hội thảo kỹ thuật quốc tế.",
                List.of("Microservices", "Docker", "RESTful API")
            ),
            new SeedJobDef(
                "Python / Django Backend Developer", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "MIDDLE", 24, 36, false,
                "Phát triển backend cho các dịch vụ mạng xã hội và phân tích dữ liệu hành vi người dùng.",
                "2+ năm kinh nghiệm Python, Django hoặc FastAPI, Redis, PostgreSQL.",
                "Lương 24 - 36 triệu, phụ cấp ăn trưa, môi trường năng động.",
                List.of("Python", "PostgreSQL", "RESTful API")
            ),
            new SeedJobDef(
                ".NET Core C# Senior Enterprise Developer", "CMC Global", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 32, 48, false,
                "Phát triển các ứng dụng quản lý doanh nghiệp quy mô lớn bằng .NET 8 / C# và Azure Cloud.",
                "4+ năm kinh nghiệm C#, ASP.NET Core, Entity Framework, SQL Server, Clean Architecture.",
                "Lương 32 - 48 triệu, bảo hiểm toàn diện, môi trường chuẩn quốc tế.",
                List.of("SQL / MySQL", "Microservices", "RESTful API")
            ),
            new SeedJobDef(
                "PHP / Laravel Senior Developer", "RikkeiSoft", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 25, 38, false,
                "Phát triển các hệ thống thương mại điện tử và CRM cho các đối tác Nhật Bản.",
                "3+ năm kinh nghiệm PHP, Laravel, MySQL, Redis. Có hiểu biết về VueJS là điểm cộng.",
                "Lương 25 - 38 triệu, thưởng dự án, phụ cấp tiếng Nhật.",
                List.of("SQL / MySQL", "HTML5 & CSS3", "RESTful API")
            ),
            new SeedJobDef(
                "C++ / Embedded Software Engineer", "FPT Software", "Đà Nẵng", "FPT Complex, Ngũ Hành Sơn",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Tham gia nghiên cứu và phát triển phần mềm nhúng điều khiển ô tô (Automotive Software) cho khách hàng Đức.",
                "2+ năm kinh nghiệm C/C++, Linux Embedded, RTOS. Tiếng Anh đọc viết tốt.",
                "Môi trường công nghệ cao tại FPT Complex Đà Nẵng, cơ hội onsite châu Âu.",
                List.of("Git / GitHub", "Linux")
            ),
            new SeedJobDef(
                "Cybersecurity Specialist & Penetration Tester", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Đánh giá an toàn thông tin, dò quét lỗ hổng và kiểm thử xâm nhập (Pen-test) cho các ứng dụng Viettel Money.",
                "3+ năm kinh nghiệm kiểm thử bảo mật. Có chứng chỉ OSCP, CEH hoặc CISSP.",
                "Lương 35 - 55 triệu. Làm việc trong trung tâm an ninh mạng hàng đầu Việt Nam.",
                List.of("Linux", "RESTful API")
            ),
            new SeedJobDef(
                "SOC Security Analyst (Giám sát an ninh mạng)", "VNPT IT", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "MIDDLE", 20, 32, false,
                "Vận hành và trực giám sát hệ thống Security Operations Center 24/7, ứng cứu sự cố an ninh mạng.",
                "2 năm kinh nghiệm giám sát an toàn thông tin, phân tích log SIEM, hiểu biết về mạng và tường lửa.",
                "Môi trường làm việc ổn định, phụ cấp ca trực và chế độ đãi ngộ tốt.",
                List.of("Linux")
            ),
            new SeedJobDef(
                "Information Security Engineer", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Bảo vệ hạ tầng thanh toán điện tử, triển khai các tiêu chuẩn bảo mật tài chính PCI-DSS Level 1.",
                "3+ năm kinh nghiệm bảo mật ứng dụng (AppSec) và hạ tầng đám mây.",
                "Lương 35 - 50 triệu. Chế độ bảo hiểm và đãi ngộ xứng đáng.",
                List.of("Linux", "AWS")
            ),
            new SeedJobDef(
                "Blockchain & Web3 Smart Contract Developer", "NextTech Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "SENIOR", 35, 60, false,
                "Nghiên cứu và phát triển các hợp đồng thông minh (Smart Contracts) trên Ethereum và Polygon.",
                "2+ năm kinh nghiệm Solidity, Web3.js, bảo mật smart contract.",
                "Thu nhập thỏa thuận theo năng lực, cơ hội tiếp cận công nghệ Blockchain tiên tiến.",
                List.of("JavaScript", "TypeScript")
            ),
            new SeedJobDef(
                "Database Administrator (DBA PostgreSQL & MySQL)", "VNG Corporation", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 50, false,
                "Tối ưu hóa, sao lưu dự phòng, phân cụm replication và đảm bảo tính toàn vẹn của hệ CSDL khổng lồ.",
                "4+ năm kinh nghiệm DBA MySQL, PostgreSQL. Hiểu sâu về phân mảnh dữ liệu (Sharding) và indexing.",
                "Môi trường đãi ngộ hàng đầu, nhiều cơ hội giải quyết bài toán Big Data.",
                List.of("SQL / MySQL", "PostgreSQL", "Linux")
            ),
            new SeedJobDef(
                "Big Data Architect (Hadoop & Hive)", "Viettel Digital", "Hà Nội", "Ba Đình, Hà Nội",
                "FULL_TIME", "SENIOR", 45, 65, false,
                "Thiết kế kiến trúc lưu trữ và xử lý dữ liệu lớn cho hàng chục triệu khách hàng viễn thông.",
                "5+ năm kinh nghiệm Big Data Architecture (Hadoop, Hive, Kafka, Flink).",
                "Lương lên đến 65 triệu. Thưởng năm vượt trội theo kết quả kinh doanh.",
                List.of("SQL / MySQL", "Python", "Microservices")
            ),
            new SeedJobDef(
                "Senior BI Analyst (Business Intelligence)", "Shopee Vietnam", "TP. Hồ Chí Minh", "Quận 1, TP. HCM",
                "FULL_TIME", "SENIOR", 32, 48, false,
                "Xây dựng mô hình phân tích kinh doanh, dự báo xu hướng thị trường và hỗ trợ ban giám đốc ra quyết định.",
                "3+ năm kinh nghiệm BI/Data Analytics. Tư duy phân tích kinh doanh nhạy bén.",
                "Môi trường quốc tế, chế độ đãi ngộ xuất sắc.",
                List.of("SQL / MySQL", "Python")
            ),
            new SeedJobDef(
                "Kỹ sư Xử lý Dữ liệu lớn (Data Warehouse)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 26, 40, false,
                "Thiết kế và duy trì kho dữ liệu Data Warehouse trên Google BigQuery phục vụ hệ thống VinID.",
                "2-3 năm kinh nghiệm Data Warehouse, ETL/ELT, SQL chuyên sâu.",
                "Lương 26 - 40 triệu, đầy đủ chế độ phúc lợi tập đoàn.",
                List.of("SQL / MySQL", "PostgreSQL", "Python")
            ),
            new SeedJobDef(
                "AI / NLP Research Engineer", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, Thủ Đức",
                "FULL_TIME", "SENIOR", 35, 55, false,
                "Nghiên cứu các thuật toán xử lý ngôn ngữ tự nhiên (NLP) tiếng Việt và tiếng Nhật phục vụ nhận dạng văn bản.",
                "Thạc sĩ hoặc 3 năm kinh nghiệm trong lĩnh vực NLP/AI. Thành thạo PyTorch và Transformers.",
                "Môi trường R&D chuyên nghiệp, tài trợ tham gia các hội nghị khoa học quốc tế.",
                List.of("Python")
            ),
            new SeedJobDef(
                "Junior Business Analyst (BA IT)", "CMC Global", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "JUNIOR", 14, 20, false,
                "Hỗ trợ ghi nhận yêu cầu khách hàng, vẽ wireframe và viết tài liệu User Story.",
                "1 năm kinh nghiệm làm BA. Tiếng Anh giao tiếp tốt là lợi thế lớn.",
                "Môi trường đào tạo chuyên nghiệp, nhiều cơ hội thăng tiến.",
                List.of("Agile / Scrum")
            ),
            new SeedJobDef(
                "Thực tập sinh UI/UX Design", "FPT Software", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "INTERN", 5, 8, false,
                "Tham gia hỗ trợ thiết kế icon, banner và layout giao diện web/app theo hướng dẫn của Lead Designer.",
                "Sinh viên yêu thích thiết kế đồ họa, biết sử dụng Figma căn bản, có mắt thẩm mỹ tốt.",
                "Trợ cấp thực tập hàng tháng, cấp máy tính làm việc, cơ hội làm việc chính thức.",
                List.of("Figma / UI-UX")
            ),
            new SeedJobDef(
                "Linux System Administrator & DevOps", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 24, 36, false,
                "Quản trị và vận hành các máy chủ Linux, thiết lập hệ thống giám sát và sao lưu dữ liệu tự động.",
                "2+ năm kinh nghiệm quản trị Linux (RHEL, Ubuntu), Shell Scripting, Docker.",
                "Lương 24 - 36 triệu, môi trường làm việc chuyên nghiệp.",
                List.of("Docker", "Git / GitHub")
            ),
            new SeedJobDef(
                "DevSecOps Specialist (Bảo mật đám mây)", "VNPT IT", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 30, 45, false,
                "Tích hợp các công cụ kiểm tra bảo mật (SAST/DAST) vào đường ống CI/CD của các sản phẩm phần mềm quốc gia.",
                "3+ năm kinh nghiệm DevOps và an toàn thông tin.",
                "Công việc ổn định lâu dài, đóng góp cho các dự án chuyển đổi số quốc gia.",
                List.of("Docker", "Git / GitHub")
            ),
            new SeedJobDef(
                "AWS Solutions Architect", "CMC Global", "Hà Nội", "Cầu Giấy, Hà Nội",
                "FULL_TIME", "SENIOR", 40, 60, false,
                "Tư vấn và thiết kế giải pháp chuyển dịch hệ thống lên AWS cho các khách hàng doanh nghiệp quốc tế.",
                "4+ năm kinh nghiệm thiết kế kiến trúc đám mây. Có chứng chỉ AWS Solutions Architect Professional.",
                "Lương 40 - 60 triệu, phụ cấp ngoại ngữ và thưởng hiệu quả kinh doanh.",
                List.of("AWS", "Docker", "Microservices")
            ),
            new SeedJobDef(
                "Senior Fullstack Web Engineer", "NashTech Vietnam", "TP. Hồ Chí Minh", "Tân Bình, TP. HCM",
                "FULL_TIME", "SENIOR", 35, 52, false,
                "Xây dựng ứng dụng tài chính trực tuyến toàn cầu cho khách hàng Anh và Bắc Âu.",
                "4+ năm kinh nghiệm Fullstack (Node/Java/C# + React/Angular). Tiếng Anh lưu loát.",
                "Đãi ngộ quốc tế, làm việc Hybrid linh hoạt.",
                List.of("Java", "React", "TypeScript", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Fullstack Developer (FinTech Ecosystem)", "NextTech Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 25, 38, false,
                "Phát triển các cổng thanh toán và ứng dụng ví điện tử phục vụ các doanh nghiệp vừa và nhỏ.",
                "2-3 năm kinh nghiệm Fullstack web (Java/Node + React).",
                "Lương 25 - 38 triệu, môi trường khởi nghiệp năng động đầy nhiệt huyết.",
                List.of("Java", "React", "SQL / MySQL")
            ),
            new SeedJobDef(
                "Flutter Developer (Ứng dụng cư dân thông minh)", "One Mount Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 24, 36, false,
                "Phát triển ứng dụng kết nối cư dân khu đô thị Vinhomes với các dịch vụ tiện ích.",
                "2+ năm kinh nghiệm Flutter, Bloc, REST API.",
                "Chế độ lương thưởng cạnh tranh, hưởng trọn vẹn quyền lợi Vingroup.",
                List.of("Git / GitHub", "RESTful API")
            ),
            new SeedJobDef(
                "Mobile Application Developer (Fintech)", "NextTech Group", "Hà Nội", "Hai Bà Trưng, Hà Nội",
                "FULL_TIME", "MIDDLE", 22, 35, false,
                "Phát triển các ứng dụng tài chính số trên nền tảng React Native.",
                "2 năm kinh nghiệm lập trình di động. Đam mê lĩnh vực công nghệ tài chính.",
                "Môi trường khuyến khích sáng tạo và thử nghiệm ý tưởng mới.",
                List.of("React", "JavaScript")
            ),
            new SeedJobDef(
                "Junior React Native Developer", "FPT Software", "TP. Hồ Chí Minh", "Khu Công Nghệ Cao, Thủ Đức",
                "FULL_TIME", "JUNIOR", 14, 20, false,
                "Tham gia bảo trì và nâng cấp các ứng dụng di động cho đối tác.",
                "1 năm kinh nghiệm React Native. Tinh thần học hỏi cao, trách nhiệm trong công việc.",
                "Môi trường FPT Software năng động, cơ hội tiếp cận nhiều dự án lớn.",
                List.of("React", "JavaScript")
            ),
            new SeedJobDef(
                "Thực tập sinh Data Engineer", "MoMo (M-Service)", "TP. Hồ Chí Minh", "Quận 7, TP. HCM",
                "FULL_TIME", "INTERN", 6, 9, false,
                "Thực tập tại khối Dữ liệu của MoMo. Tham gia xây dựng các đường ống dữ liệu thực tế.",
                "Sinh viên năm cuối ngành Khoa học dữ liệu / CNTT, thành thạo SQL và Python.",
                "Trợ cấp thực tập cạnh tranh (6 - 9 triệu), môi trường đào tạo hàng đầu.",
                List.of("SQL / MySQL", "Python")
            )
        );
    }
}

