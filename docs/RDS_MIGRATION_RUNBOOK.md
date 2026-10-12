# Kế hoạch chuyển TalentBridge sang Amazon RDS for MySQL

## Phạm vi và trạng thái đã xác minh

Backend đã dùng MySQL Connector/J và profile `prod` nhận URL, username, password qua biến môi trường. Báo cáo triển khai gần nhất trong repository ghi nhận TiDB Cloud là DB production; cần xác nhận lại trên Render/TiDB trước khi chạy vì thông tin đó có thể đã thay đổi. Workspace hiện chưa có AWS CLI profile, AWS API credentials hoặc Render API credential để kiểm tra/tạo tài nguyên cloud.

Runbook này không yêu cầu đổi mô hình Hexagonal Architecture: cấu hình DB nằm ở composition/configuration layer, migration vẫn do Flyway quản lý, còn core domain/application không phụ thuộc RDS hay Spring Data.

## Cấu hình RDS mục tiêu

1. Dùng Amazon RDS for MySQL trong `ap-southeast-1` (Singapore), gần Render backend đang ở Singapore. Chọn phiên bản MySQL 8 tương thích với schema hiện tại.
2. Chốt instance, storage, backup retention và giá dự kiến trong AWS Pricing Calculator trước khi tạo. Single-AZ có thể là cấu hình khởi điểm chi phí thấp cho dự án demo; không coi Free Tier là chắc chắn nếu chưa kiểm tra điều kiện tài khoản. Bật encryption at rest, automated backups, deletion protection và đặt retention tối thiểu 7 ngày.
   - Mốc tham khảo từ AWS Price List `ap-southeast-1`, kiểm tra ngày 12/10/2026: `db.t4g.micro` Single-AZ là `$0.025/giờ` (khoảng `$18.25` cho 730 giờ); gp3 MySQL là `$0.138/GiB-tháng`, nên 20 GiB khoảng `$2.76`; tổng cơ sở của cấu hình mẫu 20 GiB là **khoảng `$21.01/tháng`**. Đây chưa phải báo giá cuối: chưa gồm backup vượt mức miễn phí, data transfer, thuế, và CPU credits T4g khi vượt baseline (`$0.075/vCPU-giờ`). Kiểm tra kích thước DB nguồn trước khi chọn 20 GiB; nếu vượt thì tính lại. Giá có thể thay đổi, hãy kiểm tra lại bằng [AWS Pricing Calculator](https://calculator.aws/) trước lúc tạo. Nguồn: [AWS Price List cho RDS tại Singapore](https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonRDS/current/ap-southeast-1/index.json), [RDS MySQL pricing](https://aws.amazon.com/rds/mysql/pricing/).
3. Tạo DB name khớp với schema nguồn sau khi đọc giá trị datasource hiện tại; không suy ra tên DB từ tài liệu cũ. Không dùng RDS master user trong ứng dụng. Tạo tài khoản runtime riêng cho CRUD và tài khoản migration riêng có quyền DDL mà Flyway cần trên đúng database. Cấu hình chúng bằng `SPRING_DATASOURCE_USERNAME/PASSWORD` và `SPRING_FLYWAY_USER/PASSWORD`; nếu hai biến Flyway chưa đặt, ứng dụng tương thích ngược bằng cách dùng datasource account.
4. RDS phải có kết nối từ Render backend. Nếu dùng public endpoint, chỉ bật TLS và chỉ cho phép port `3306` từ outbound CIDR của đúng Render service tại **Connect → Outbound**. Các CIDR mặc định của Render được dùng chung trong region; nếu yêu cầu allowlist riêng, cần dedicated outbound IP (dịch vụ tính phí) hoặc đổi kiến trúc để app và RDS cùng private network. Không mở `3306` cho `0.0.0.0/0`.
5. Trong URL production, bật `sslMode=VERIFY_IDENTITY`, dùng endpoint DNS của RDS, và truststore `/app/rds-ca-truststore.p12` đã được đóng gói vào image. Ví dụ, thay placeholder bằng endpoint/schema thật:

   ```text
   jdbc:mysql://<rds-endpoint>:3306/<source-schema>?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=file:/app/rds-ca-truststore.p12&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
   ```

   Mật khẩu truststore ở đây chỉ bảo vệ truststore chứa CA public, không phải mật khẩu database. Lưu database password riêng trong Render Environment; không đưa secret vào Git, chat, URL/query log hoặc shell history.

## Cutover có kiểm soát

1. Ghi lại các giá trị đối soát ở nguồn: Flyway version, danh sách bảng và `COUNT(*)` từng bảng, cùng các tổng hợp không chứa PII (users, companies, jobs theo status, applications theo stage). Kiểm tra timezone, collation, charset, index, foreign key và bảng `flyway_schema_history`.
2. Tạo snapshot/backup của TiDB và thử đọc backup. Lưu bản dump chứa dữ liệu cá nhân ở nơi mã hóa, quyền truy cập giới hạn; không upload qua chat.
3. Tạo RDS và kiểm tra kết nối TLS từ backend network. Tạo database theo đúng tên nguồn.
4. Đưa backend vào maintenance/đóng ghi trong cửa sổ cutover. Dùng `mysqldump`/MySQL Shell tương thích TiDB để xuất schema và dữ liệu, bao gồm `flyway_schema_history`; khôi phục dump vào RDS. Không chạy `database/schema.sql` trên dữ liệu đã import vì file đó không phải luồng khôi phục dữ liệu production.
5. Đối chiếu từng bảng nguồn/đích, migration version, foreign-key checks và tổng hợp nghiệp vụ. Kiểm tra các quan hệ users/roles, resumes/applications, companies/jobs bằng SQL read-only. Đảm bảo các primary key, AUTO_INCREMENT và lịch sử moderation được giữ nguyên.
6. Cập nhật `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` và, nếu tách quyền, `SPRING_FLYWAY_USER/PASSWORD` trên Render. Không đưa password vào `render.yaml`. Vì Blueprint đặt `autoDeploy: false`, deploy backend có chủ đích sau khi import/đối soát đã đạt.
7. Smoke test `/api/v1/health`, `/api/v1/jobs`, đăng nhập bằng tài khoản kiểm thử, xem hồ sơ/đơn ứng tuyển phù hợp, tạo một bản ghi test được phê duyệt rồi xóa theo quy trình test. Đọc log khởi động xác nhận Flyway validate và Hibernate schema validation không lỗi; kiểm tra kết nối TLS đang dùng.
8. Chỉ mở ghi lại khi checks đạt. Theo dõi lỗi DB, latency và số liệu nghiệp vụ. Nếu cần rollback trước khi mở ghi, chuyển datasource về TiDB. Sau khi đã có ghi mới trên RDS, không rollback một chiều vì sẽ làm mất các thay đổi mới; phải giữ maintenance hoặc đồng bộ ngược trước.
9. Giữ TiDB và backup trong thời gian soak đã thống nhất. Sau khi xác nhận khôi phục backup được và không còn ứng dụng nào dùng TiDB, xóa tài nguyên TiDB như một thao tác riêng có xác nhận ngay trước khi thực hiện. Tắt/giảm dịch vụ chưa đủ để chứng minh dữ liệu đã được xóa.

## Hạn chế hiện tại

- Không có AWS API/CLI credential được cấu hình trong môi trường làm việc; chưa có RDS endpoint, instance, security group hay estimate giá thực tế.
- Chưa truy cập được secret datasource của Render hoặc TiDB để dump dữ liệu. Không yêu cầu gửi password vào chat; cần dashboard session hoặc người dùng tự cấu hình secrets.
- Vì chưa có RDS và dump nguồn, chưa thể khẳng định data migration, production deploy hoặc việc xóa TiDB đã hoàn tất.

## Kết quả preflight chỉ đọc - 12/10/2026

- Trên TiDB Cloud, cluster `talentbridge` đang **Active**, plan **STARTER**, vùng **Tokyo (`ap-northeast-1`)**, tương thích MySQL; Overview báo **Row-based Storage: 0 MiB** và **Columnar Storage: 0 MiB**.
- SQL Editor liệt kê schema nghiệp vụ `test`. Truy vấn chỉ đọc `information_schema.tables` trả về **29 bảng**, tổng `TABLE_ROWS` ước tính **53**, tổng `data_length + index_length` **2,304 byte**. Đây là số liệu metadata/ước tính, không thay thế dump hoặc kiểm đếm chính xác từng bảng.
- AWS RDS inventory được kiểm tra trong hai vùng Singapore (`ap-southeast-1`) và Sydney (`ap-southeast-2`); cả hai đều hiển thị **0 databases**.
- Kích thước hiện thấy nhỏ hơn đáng kể so với cấu hình mẫu 20 GiB, nhưng schema tên `test` cùng số dòng ước tính thấp là dấu hiệu cần xác nhận đây đúng là nguồn đang phục vụ production và có đủ dữ liệu nghiệp vụ mong muốn. Chưa được cutover chỉ dựa trên số liệu này.
- Chưa có bằng chứng về snapshot/export đã kiểm thử phục hồi, số dòng chính xác theo từng bảng, hoặc dữ liệu production. TiDB vẫn là nguồn hiện tại và không bị thay đổi/xóa trong preflight.

## Tài liệu kỹ thuật tham khảo

- [Amazon RDS for MySQL pricing](https://aws.amazon.com/rds/mysql/pricing/) — phí instance-hours, storage, backups và CPU credits.
- [Tạo DB instance RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html) và [thiết lập public/private access](https://docs.aws.amazon.com/AmazonRDS/latest/gettingstartedguide/security-public-private.html).
- [AWS RDS SSL/TLS certificate bundles](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.SSL.html) — bundle riêng cho Singapore là `ap-southeast-1-bundle.pem`.
- [MySQL Connector/J SSL modes](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html) — `VERIFY_IDENTITY` kiểm tra TLS và hostname.
- [Render outbound IP ranges](https://render.com/docs/outbound-ip-addresses) và [dedicated IPs](https://render.com/docs/dedicated-ips) — default ranges dùng chung theo region; dedicated outbound IP có phí và yêu cầu Pro trở lên.
