# API metrics trong cổng quản trị

## Endpoint và quyền truy cập

`GET /api/v1/admin/metrics/api?hours=24` trả thống kê đã gom cho Admin. `hours` được giới hạn từ 1 đến 24. Trang giao diện nằm tại `/admin/metrics`.

## Quy tắc thu thập

- Chỉ đo đường dẫn `/api/v1/**`; loại trừ health check và endpoint metrics để request kiểm tra không làm sai số.
- Mỗi request được gắn với method và route template của Spring MVC, ví dụ `GET /api/v1/jobs/{id}`. Nếu chưa ánh xạ được handler thì gộp thành `/_unmatched-api-route`, tránh lưu ID, email hoặc slug tùy ý.
- 4xx và 5xx được tính là lỗi. Tỷ lệ lỗi = `(4xx + 5xx) / tổng request`. Độ trễ gồm thời gian xử lý phía server qua filter; báo trung bình và lớn nhất, không suy diễn p95.
- Gom theo bucket 5 phút. Không lưu URL query, body, token, tài khoản hay nội dung request.
- Dùng bộ đếm đồng thời trong bộ nhớ để không ghi DB trên đường xử lý request. Bucket cũ hơn 24 giờ được dọn định kỳ mỗi phút và khi đọc/ghi.
- Số liệu chỉ tồn tại trong tiến trình hiện tại, mất khi restart/redeploy; mỗi instance giữ bộ đếm riêng. Nếu cần lưu lịch sử qua restart hoặc hợp nhất nhiều instance, cần gắn Prometheus/OTel và kho giám sát ngoài trước khi mở rộng dịch vụ.

## Kiểm thử

- Unit test xác nhận bucket, 4xx/5xx, trung bình/cao nhất và giới hạn lưu giữ.
- Filter test xác nhận route được gom theo template và URL chưa ánh xạ không để lộ dữ liệu đường dẫn.
- Spring MVC test xác nhận endpoint cần đăng nhập, chỉ Admin đọc được và dữ liệu được trả theo endpoint/bucket.
