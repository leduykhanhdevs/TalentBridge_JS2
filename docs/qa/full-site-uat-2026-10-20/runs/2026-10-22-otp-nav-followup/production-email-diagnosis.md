# Chẩn đoán email đặt lại mật khẩu trên Render — 2026-10-22

## Quan sát

Trong Render backend logs, yêu cầu đặt lại mật khẩu ghi lỗi `MailConnectException` khi ứng dụng kết nối `smtp.gmail.com:587`; nguyên nhân là `java.net.ConnectException: Operation timed out`. Không có bằng chứng SMTP chấp nhận thư, vì vậy giao diện không nên khẳng định OTP đã gửi.

## Nguyên nhân

Tài liệu Render hiện hành ghi Free web services không thể gửi outbound qua các cổng `25`, `465`, `587`, là các cổng SMTP phổ biến. Đây là giới hạn mạng của gói Free; timeout không chỉ ra Gmail username/app password sai. Nguồn: [Render Free web services](https://render.com/docs/free), [Render changelog: SMTP ports](https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports).

## Thay đổi và giới hạn

- Đã thêm timeout kết nối/đọc/ghi SMTP 10 giây để request không quay quá lâu.
- API/UI báo dịch vụ gửi mail tạm thời không khả dụng khi backend trả 503.
- Màn OTP dùng nội dung có điều kiện và không nói thư chắc chắn đã được gửi.
- Thay đổi trên mới ở nhánh PR #54; chưa xác nhận đã lên production.
- Các thay đổi này không bỏ được giới hạn mạng của Render Free và không chứng minh email thật đã giao thành công.

## Cách hoàn tất gửi mail thật

1. Chuyển backend sang compute plan có outbound SMTP, nếu tiếp tục dùng Gmail SMTP; hoặc
2. Tích hợp một email provider có API HTTPS và cấu hình API key qua Render Environment. Không ghi key vào Git hoặc log.

Sau khi cấu hình, kiểm tra health/deploy SHA rồi gửi một OTP thử nghiệm có kiểm soát và xác nhận thư thực nhận. Chưa thực hiện gửi mail đến hộp thư cá nhân trong lần UAT này.
