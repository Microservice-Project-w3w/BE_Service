# Role-permission matrix

| Role | Nhóm permission chính | Data scope |
| --- | --- | --- |
| `ADMIN` | Toàn bộ permission; quản lý doanh nghiệp, chi nhánh, tài khoản, nhân viên, cấu hình và báo cáo | `SYSTEM` |
| `MANAGER` | Đọc thiết bị, khách hàng, đơn thuê, giao nhận, công nợ; phê duyệt báo giá và hợp đồng | `BRANCH` |
| `SALES_STAFF` | Khách hàng, yêu cầu thuê, báo giá, đơn thuê, hợp đồng (không tự phê duyệt) | `BRANCH` |
| `OPERATIONS_STAFF` | Thiết bị, kho, nhập xuất, điều chuyển, kiểm kê, giao nhận, nhận trả, bảo trì, sửa chữa và đánh giá hư hỏng | `BRANCH` |
| `ACCOUNTANT` | Hóa đơn, thanh toán, cọc, hoàn tiền, khấu trừ, công nợ và báo cáo | `ORGANIZATION` hoặc `BRANCH` theo assignment |
| `CUSTOMER` | Dữ liệu công khai và dữ liệu của chính mình; yêu cầu thuê/trả, báo giá, hợp đồng, hóa đơn, thanh toán, sự cố | `OWN` |

Khi service khởi động, các role cũ được chuyển lần lượt thành: `SUPER_ADMIN` và `ORG_ADMIN` → `ADMIN`; `BRANCH_MANAGER` → `MANAGER`; `WAREHOUSE_STAFF`, `DELIVERY_STAFF` và `TECHNICIAN` → `OPERATIONS_STAFF`. File [role-permission-seed.csv](role-permission-seed.csv) là mapping ban đầu; endpoint vẫn phải kiểm tra data scope.
