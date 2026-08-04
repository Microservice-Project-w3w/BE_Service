# Quyền sở hữu database

Database dự kiến theo service:

- `identity_db`
- `organization_customer_db`
- `inventory_db`
- `rental_db`
- `logistics_db`
- `billing_db`
- `maintenance_db`

Mỗi service chỉ truy cập database của chính mình. Ở môi trường local có thể dùng cùng MySQL server, nhưng vẫn phải tách database/schema. Không JOIN xuyên database, không tạo JPA relationship đến entity thuộc service khác, và không tạo foreign key xuyên service.

ID của service khác chỉ được lưu dưới dạng `Long`, `UUID` hoặc kiểu ID phù hợp. Khi cần dữ liệu chi tiết, service phải gọi API của service sở hữu dữ liệu đó.
