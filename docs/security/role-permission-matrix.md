# Role-permission matrix

| Role | Nhóm permission chính | Data scope |
| --- | --- | --- |
| `SUPER_ADMIN` | Toàn bộ permission | `SYSTEM` |
| `ORG_ADMIN` | Quản trị organization, customer, inventory, rental, logistics, billing và maintenance trong tổ chức | `ORGANIZATION` |
| `BRANCH_MANAGER` | Đọc thiết bị, khách hàng, đơn thuê, giao nhận, công nợ; phê duyệt báo giá và hợp đồng | `BRANCH` |
| `SALES_STAFF` | Khách hàng, yêu cầu thuê, báo giá, đơn thuê, hợp đồng (không tự phê duyệt) | `BRANCH` |
| `WAREHOUSE_STAFF` | Danh mục, thiết bị, kho, nhập xuất, điều chuyển, kiểm kê và QR | `BRANCH` |
| `DELIVERY_STAFF` | Nhiệm vụ giao nhận được giao, bàn giao và nhận trả | `OWN` hoặc `BRANCH` theo endpoint |
| `ACCOUNTANT` | Hóa đơn, thanh toán, cọc, hoàn tiền, khấu trừ, công nợ và báo cáo | `ORGANIZATION` hoặc `BRANCH` theo assignment |
| `TECHNICIAN` | Bảo trì, sửa chữa, sự cố, linh kiện và đánh giá hư hỏng | `OWN` hoặc `BRANCH` |
| `CUSTOMER` | Dữ liệu công khai và dữ liệu của chính mình; yêu cầu thuê/trả, báo giá, hợp đồng, hóa đơn, thanh toán, sự cố | `OWN` |

`ORG_ADMIN` không có các thao tác cấp hệ thống dành riêng cho `SUPER_ADMIN`. File [role-permission-seed.csv](role-permission-seed.csv) là mapping ban đầu; endpoint vẫn phải kiểm tra data scope.
