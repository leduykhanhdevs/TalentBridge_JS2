# HRPM-12 — Logout & Session Lifecycle

- Người thực hiện: Phan Thị Ánh Tuyền.
- Nguồn yêu cầu: Jira HRPM-12.
- Trạng thái backend: Đã triển khai và kiểm thử.
- Trạng thái frontend: Đã thống nhất contract; chưa tích hợp vì frontend đang ở HRPM-5.
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

## 4. Quy tắc vòng đời phiên đã triển khai

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

## 5. Contract tích hợp frontend

Frontend hiện chưa triển khai login và auth store vì đang ở giai đoạn
setup nền tảng HRPM-5. Hai bên đã thống nhất contract tích hợp sau:

- Dùng Zustand auth store và persist vào localStorage.
- Chỉ sử dụng một key là `talentbridge-auth`.
- Key lưu access token, refresh token và thông tin user.
- Không tạo các key access token hoặc refresh token riêng.
- Logout gọi `POST /api/v1/auth/logout`.
- Gửi access token trong header `Authorization: Bearer <accessToken>`.
- Không gửi request body.

Sau khi logout:

- HTTP 200: gọi `clearAuth()`, xóa state xác thực,
  dữ liệu persist và cache riêng của người dùng;
  sau đó chuyển đến `/login` bằng replace navigation.
- HTTP 401: thử refresh tối đa một lần.
- Refresh thành công: cập nhật cặp token mới
  và gọi lại logout đúng một lần.
- Refresh trả HTTP 401: xóa thông tin xác thực
  và chuyển đến `/login`.
- Lỗi mạng: không coi là máy chủ đã thu hồi phiên thành công;
  giữ trạng thái hiện tại, thông báo lỗi và cho phép thử lại.
- Frontend phải có cờ retry để tránh vòng lặp
  refresh/logout hoặc gửi logout nhiều lần.

Việc chuyển sang refresh token bằng HttpOnly Secure SameSite Cookie
được ghi nhận là cải tiến tương lai và không thuộc phạm vi HRPM-12.

## 6. Kết quả kiểm thử

- [x] Logout hợp lệ trả HTTP 200.
- [x] Access token của phiên vừa logout gọi API bảo vệ nhận HTTP 401.
- [x] Refresh token của phiên vừa logout bị từ chối.
- [x] Access token hết hạn gọi API bảo vệ nhận HTTP 401.
- [x] Phiên hết hạn không thể được làm mới.
- [x] Refresh hợp lệ trả cặp token mới.
- [x] Refresh token cũ không thể sử dụng lại.
- [x] Gửi sai loại token bị từ chối.
- [x] Logout một phiên không thu hồi phiên khác.
- [x] Trạng thái thu hồi được lưu trong cơ sở dữ liệu.
- [x] Token mới không vượt quá thời hạn cố định của phiên.
- [x] Toàn bộ 81 test backend đạt, không có failure hoặc error.
- [ ] Giao diện xóa auth state và chuyển về `/login`.
  Mục này chờ frontend triển khai authentication sau HRPM-5.