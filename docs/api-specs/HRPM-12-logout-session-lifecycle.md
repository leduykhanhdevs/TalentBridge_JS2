# HRPM-12 — Logout & Session Lifecycle

- Người thực hiện: Phan Thị Ánh Tuyền.
- Nguồn yêu cầu: Jira HRPM-12.
- Trạng thái backend: Đã triển khai và kiểm thử.
- Trạng thái frontend: Đã tích hợp logout/refresh có deadline và phối hợp giữa các tab; production rollout theo PR/CD.
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
- Header tùy chọn: `Authorization: Bearer <accessToken>` khi không có refresh token.
- Request body tùy chọn: `{"refreshToken":"<refreshToken>"}`. Nếu không có access token hợp lệ, refresh token phải còn là token hiện hành trong session để thu hồi; token cũ đã bị xoay không thể tự thu hồi phiên và cũng không thể dùng để refresh.
- Cần ít nhất một token hợp lệ; nếu gửi cả hai token hợp lệ thì chúng phải cùng session.
- Path parameters và query parameters: không có.
- Áp dụng cho mọi vai trò đã xác thực.

Lựa chọn triển khai:
Máy chủ xác minh chữ ký, thời hạn, loại token, session ID và user ID. Nếu cả access token và refresh token đều hợp lệ, chúng phải thuộc cùng một phiên. Access token hợp lệ có thể thu hồi phiên của chính nó. Refresh token chỉ được dùng để kết thúc phiên khi hash còn khớp với refresh token hiện hành; token cũ đã bị xoay không thể tự thu hồi và không cấp quyền refresh hay quyền nghiệp vụ. Frontend đặt logout intent dùng chung giữa các tab và che trạng thái đăng nhập ngay; sau đó lấy Web Lock `talentbridge-auth-session`, cùng khóa với thao tác refresh. Refresh đang giữ khóa được phép hoàn tất và lưu cặp token mới nhất; logout lấy khóa sau đó, đọc cặp hiện hành, xóa auth local và gửi revoke đúng một lần. Refresh đang chờ khóa sẽ dừng khi thấy logout intent. Các phiên đăng nhập khác của cùng tài khoản vẫn hoạt động.

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

Frontend hiện triển khai auth token trong localStorage và đồng bộ trạng thái đăng xuất
giữa các tab bằng `talentbridge_logout_intent` cùng event `storage`.

Sau khi logout:

- Khi người dùng chọn logout, đặt logout intent để các tab che thông tin tài khoản ngay;
  điều hướng UI không chờ request mạng.
- Refresh, logout và lưu phiên đăng nhập mới dùng chung Web Lock
  `talentbridge-auth-session`. Logout chờ lock tối đa bằng thời hạn refresh, đọc cặp
  token mới nhất, xóa token người dùng và giới hạn mỗi request revoke bằng timeout riêng.
- Refresh mới thấy logout intent sẽ dừng trước khi gọi backend. Refresh đã bắt đầu trước
  intent có thể hoàn tất dưới lock; logout sẽ thu hồi cặp token vừa xoay trước khi nhả lock.
- Nếu lock acquisition từ chối hoặc timeout trước callback, frontend dùng cặp token đang
  lưu khi logout intent còn hiệu lực, xóa auth local và gửi một request revoke dự phòng
  có timeout hữu hạn. Nếu revoke trong callback đã được bắt đầu nhưng lỗi mạng/backend,
  không gửi request trùng; local auth vẫn bị xóa và không được ghi nhận là server đã
  thu hồi thành công.
- Trình duyệt không hỗ trợ Web Locks sẽ không refresh token để tránh race liên tab; thao tác
  logout vẫn xóa auth local và thử revoke phiên theo cơ chế best-effort có timeout.

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
- [x] Giao diện xóa auth state và điều hướng về `/login` ngay, không chờ phản hồi mạng.
- [x] Lock reject/timeout vẫn xóa auth local và gửi một lần revoke dự phòng có timeout.
