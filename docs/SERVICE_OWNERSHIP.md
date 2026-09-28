# Working agreement

1. Mỗi người chỉ sửa module mình sở hữu; thay đổi Gateway/AI do owner phụ trách.
2. Không đổi public path hoặc DTO của service khác mà không cập nhật contract trong pull request.
3. Không tạo dependency Maven giữa các domain service.
4. Mỗi PR phải chạy `mvn -pl <module> test`; Gateway routes chỉ merge sau khi downstream path đã có test.
5. Database migration của từng service đặt tại `src/main/resources/db/migration` trong module đó; không dùng `ddl-auto=update` ở môi trường chia sẻ.

## Gateway namespaces

| Service | Public paths |
|---|---|
| Identity | `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**`, `/api/v1/sessions/**` |
| Organization-Customer | `/api/v1/organizations/**`, `/api/v1/branches/**`, `/api/v1/employees/**`, `/api/v1/customers/**` |
| Inventory | `/api/v1/inventory/**` |
| Rental | `/api/v1/rental-requests/**`, `/api/v1/quotations/**`, `/api/v1/rental-orders/**`, `/api/v1/rental-contracts/**`, `/api/v1/availability/**` |
| AI | `/api/v1/ai/**` (Gateway rewrites to `/api/v1/**` in AI service) |
