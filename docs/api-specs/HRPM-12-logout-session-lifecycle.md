# HRPM-12 — Logout & Session Lifecycle

- Người thực hiện: Phan Thị Ánh Tuyền.
- Nguồn yêu cầu: Jira HRPM-12.
- Trạng thái: Đặc tả dự kiến, chưa triển khai.
- HRPM-12 là đăng xuất và quản lý vòng đời phiên.
  HRPM-11 là quên và đặt lại mật khẩu.

## 1. Tiêu chí nghiệm thu từ Jira

1. Đăng xuất phải vô hiệu hóa token hoặc phiên ở phía máy chủ.
2. Sau khi đăng xuất thành công, giao diện chuyển về trang đăng nhập.
3. Token hết hạn gọi API được bảo vệ phải nhận HTTP 401.
4. Quy tắc làm mới token và hết hạn phiên phải được ghi lại và thực thi.

## 2. API đăng xuất

- Method: POST
- Endpoint: /api/v1/auth/logout
- Header bắt buộc: Authorization: Bearer <accessToken>
- Request body: không có.
- Path parameters và query parameters: không có.
- Áp dụng cho mọi vai trò đã xác thực.

Lựa chọn triển khai:
Thu hồi phiên tương ứng với access token được gửi lên.
Các phiên đăng nhập khác của cùng tài khoản vẫn hoạt động.

### Thành công — HTTP 200

Chỉ trả thành công sau khi máy chủ đã lưu trạng thái thu hồi phiên.

Ví dụ response theo cấu trúc ApiResponse hiện có:

```json
{
  "statusCode": 200,
  "message": "Đăng xuất thành công",
  "timestamp": "2026-09-15 11:40:00"
}
```

Không trả trường data khi không có dữ liệu.

### Không được xác thực — HTTP 401

Áp dụng khi thiếu token, token sai, hết hạn, sai loại,
hoặc phiên tương ứng đã bị thu hồi hay hết hạn.

Phản hồi theo cấu trúc ApiResponse của lớp bảo mật hiện có.
Đối với API được bảo vệ, mã trong body là statusCode: 40101.

## 3. API làm mới token hiện có

- Method: POST
- Endpoint: /api/v1/auth/refresh-token
- Header: Content-Type: application/json
- Không yêu cầu access token còn hạn.
- Path parameters và query parameters: không có.

Request body:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Response:
- HTTP 200: giữ cấu trúc ApiResponse<AuthResponse> hiện có.
  data gồm accessToken, refreshToken, tokenType, expiresInMs và user.
- HTTP 400: dữ liệu đầu vào thiếu hoặc không hợp lệ.
- HTTP 401: refresh token sai, hết hạn, đã sử dụng,
  hoặc thuộc phiên đã bị thu hồi hay hết hạn.
- HTTP 403: tài khoản bị khóa, theo hành vi hiện có.

## 4. Quy tắc vòng đời phiên dự kiến

- Mỗi lần đăng nhập hoặc đăng ký có cấp token tạo một phiên riêng.
- Access token và refresh token cùng thuộc một phiên.
- Máy chủ lưu trạng thái phiên trong cơ sở dữ liệu.
- Mỗi yêu cầu được bảo vệ và mỗi lần refresh đều kiểm tra phiên.
- Phiên đã thu hồi phải tiếp tục bị từ chối sau khi máy chủ khởi động lại.

Thời hạn:
- Dùng cấu hình access token hiện có: tối đa 24 giờ.
- Dùng cấu hình refresh hiện có làm giới hạn phiên: 7 ngày.
- Lựa chọn cho HRPM-12: giới hạn 7 ngày tính từ lúc tạo phiên,
  không kéo dài giới hạn này khi refresh.
- Token mới không được có hạn sử dụng vượt quá giới hạn phiên.
- Thời điểm hiện tại bằng hoặc vượt thời điểm hết hạn là hết hạn.
- expiresInMs phản ánh thời gian còn lại thực tế của access token.

Làm mới token:
- Phân biệt rõ access token và refresh token.
- Không dùng refresh token để gọi API được bảo vệ.
- Không dùng access token để gọi API refresh.
- Refresh thành công cấp cặp token mới trong cùng phiên.
- Refresh token cũ không được dùng lại.
- Việc kiểm tra và thay refresh token phải thực hiện nguyên tử,
  để hai yêu cầu đồng thời không cùng sử dụng thành công một token.

Đăng xuất:
- Thu hồi phiên hiện tại ở máy chủ.
- Mọi access token và refresh token thuộc phiên đó mất hiệu lực.
- Việc thu hồi toàn bộ phiên sau đặt lại mật khẩu sẽ thuộc HRPM-11.

## 5. Hành vi phía giao diện

- Sau khi logout nhận HTTP 200, xóa thông tin đăng nhập
  đang lưu ở client và chuyển về trang đăng nhập.
- Nếu access token hết hạn, thử refresh tối đa một lần
  rồi gọi lại logout.
- Nếu refresh bị từ chối vì phiên không còn hợp lệ,
  xóa thông tin đăng nhập và chuyển về trang đăng nhập.
- Lỗi mạng không được coi là máy chủ đã thu hồi phiên thành công.
- Phải kiểm tra hành vi chuyển trang khi tích hợp frontend;
  kiểm thử backend đơn thuần chưa chứng minh tiêu chí này đã hoàn thành.

## 6. Các kiểm thử cần bổ sung

- Logout hợp lệ trả HTTP 200.
- Access token của phiên vừa logout gọi /api/v1/auth/me nhận 401.
- Refresh token của phiên vừa logout bị từ chối.
- Access token hết hạn gọi API được bảo vệ nhận 401.
- Phiên hết hạn không thể refresh.
- Refresh hợp lệ trả cặp token mới; refresh token cũ bị từ chối.
- Gửi nhầm loại token bị từ chối.
- Đăng xuất một phiên không thu hồi phiên khác.
- Trạng thái thu hồi được lưu bền vững.
- Giao diện chuyển về trang đăng nhập sau khi đăng xuất.