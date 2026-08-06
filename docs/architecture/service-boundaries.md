# Biên giới service

## identity-service

- Đăng ký, đăng nhập, đăng xuất, refresh token, quên mật khẩu, đổi mật khẩu
- User, role, permission và session đăng nhập

## organization-customer-service

- Doanh nghiệp, chi nhánh, nhân viên và phân công nhân viên theo chi nhánh
- Khách hàng cá nhân, khách hàng doanh nghiệp, nhóm khách hàng và khách hàng hạn chế

## inventory-service

- Nhóm thiết bị, loại thiết bị, thương hiệu, model, thông số kỹ thuật, thiết bị cụ thể
- Serial, IMEI, MAC, hình ảnh, phụ kiện và QR
- Kho, nhập kho, xuất kho, điều chuyển, kiểm kê và tìm kiếm thiết bị
- Kiểm tra khả dụng, giữ chỗ và giải phóng giữ chỗ thiết bị

## rental-service

- Bảng giá thuê theo giờ, ngày, tuần, tháng; chính sách cọc và phí trả trễ; mã giảm giá
- Yêu cầu thuê, báo giá, phê duyệt báo giá, đơn thuê
- Hợp đồng, phụ lục, gia hạn, hủy và thanh lý

## logistics-service

- Phí giao nhận theo chính sách, phiếu xuất cho thuê, phân công giao nhận và lịch giao
- Checklist giao nhận, biên bản bàn giao, xác nhận giao
- Yêu cầu trả, kiểm tra khi trả và biên bản nhận trả

## billing-service

- Tính tổng tiền, phí phát sinh, hóa đơn và thanh toán nhiều đợt
- Tiền đặt cọc đã thu, hoàn cọc, khấu trừ cọc, công nợ và trạng thái thanh toán

## maintenance-service

- Lịch và cảnh báo bảo trì, phiếu bảo trì, phiếu sửa chữa, kỹ thuật viên, linh kiện thay thế
- Lịch sử kỹ thuật, báo cáo sự cố, đánh giá hư hỏng và chi phí bồi thường

## Quy tắc ở vùng giao nhau

- `inventory-service` sở hữu trạng thái vật lý và khả dụng của thiết bị; `rental-service` gọi service này để kiểm tra và giữ chỗ.
- `logistics-service` ghi nhận kết quả giao và nhận trả; `maintenance-service` đánh giá hư hỏng và chi phí kỹ thuật.
- `billing-service` tạo khoản phải thu, hóa đơn, khấu trừ và hoàn tiền.
- Không service nào sao chép toàn bộ entity của service khác; chỉ lưu ID tham chiếu khi cần.
