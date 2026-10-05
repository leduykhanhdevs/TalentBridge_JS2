# QUY TẮC ĐÓNG GÓI NGỮ CẢNH: WORKING CAPSULE PROTOCOL

---

## 1. NGUYÊN TẮC CỐT LÕI (CORE PRINCIPLE)
Khi phiên làm việc kéo dài, context bị phân mảnh hoặc hệ thống kích hoạt cơ chế nén ngữ cảnh (Context Truncation / Summarization), Agent **BẮT BUỘC** phải đóng gói trạng thái làm việc thành một **WORKING CAPSULE** ngắn gọn, súc tích và có cấu trúc chuẩn 8 mục.

---

## 2. CẤU TRÚC CHUẨN 8 MỤC BẮT BUỘC (MANDATORY 8-SECTION SCHEMA)

Mọi bản tóm tắt hoặc phục hồi ngữ cảnh phải tuân thủ nghiêm ngặt định dạng sau:

```markdown
### ⚡ WORKING CAPSULE
- **OBJECTIVE**: Mục tiêu hiện tại của task đang giải quyết.
- **DONE**: Những gì đã thực sự hoàn thành và đã được xác minh/kiểm thử thực tế.
- **CONSTRAINTS**: Các ràng buộc kỹ thuật, quy tắc bất biến (ví dụ: JVM memory, secret isolation, CORS, REST status code).
- **DECISIONS**: Quyết định kỹ thuật và kiến trúc đã chốt, không lật lại nếu không có lý do mới.
- **FILES/ARTIFACTS**: Danh sách tệp, mã nguồn, tài liệu trực tiếp liên quan.
- **ERRORS/SCARS**: Lỗi đã gặp, nguyên nhân gốc rễ, cách đã xử lý và điều TUYỆT ĐỐI KHÔNG ĐƯỢC lặp lại.
- **OPEN ITEMS**: Những hạng mục còn dang dở, chưa thực hiện hoặc đang chờ phản hồi.
- **NEXT ACTION**: Hành động cụ thể, đơn lẻ tiếp theo cần thực thi ngay.
```

---

## 3. DANH MỤC CÁC THÔNG TIN LOẠI BỎ (PRUNING RULES)

Để giữ cho context sạch và tối ưu token, Agent **TRIỆT ĐỂ LOẠI BỎ**:
- ❌ Các đoạn hội thoại trao đổi lặp đi lặp lại.
- ❌ Giải thích dài dòng lý thuyết đã hết giá trị sử dụng.
- ❌ Các phương án kỹ thuật đã bị từ chối hoặc bị loại bỏ.
- ❌ Nhật ký log kiểm thử hoặc stack trace dài (chỉ giữ thông tin lỗi cốt lõi trong mục `ERRORS/SCARS`).
- ❌ Nội dung mã nguồn dài có thể đọc trực tiếp từ tệp tin nguồn (chỉ ghi đường dẫn tệp kèm số dòng).

---

## 4. QUY TRÌNH ÁP DỤNG TRONG PHIÊN LÀM VIỆC (LIFECYCLE INTEGRATION)

1. **Bắt đầu phiên làm việc / Sau tóm tắt**:
   - Đọc lại toàn bộ source code, docs, .md, README và WORKING CAPSULE gần nhất.
2. **Khi gặp sự cố hoặc chuyển giao task**:
   - Cập nhật mục `ERRORS/SCARS` ngay lập tức để ghi nhận "vết sẹo" kỹ thuật.
3. **Trước khi thực hiện bước tiếp theo**:
   - Xác định rõ ràng duy nhất 1 `NEXT ACTION` có tính hành động cao.
