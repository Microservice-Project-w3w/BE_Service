# Quy tắc làm việc và contract

## Chỉ được sửa trong phạm vi

- Owner Gateway/AI: `api-gateway`, `ai-service`, `docs/`.
- Mỗi domain owner: đúng folder service của mình, migration và test của service đó.
- Không copy entity/repository từ service khác, không mở kết nối sang database khác.
- Không sửa Gateway route để “cho chạy tạm” trước khi downstream controller có test.

## Quy tắc chuyển code từ backend cũ

1. Bắt đầu tại controller cũ được liệt kê trong root README, lần theo DTO → service → repository → entity → test.
2. Chuyển **use case và contract**, không copy nguyên package/config/security cũ.
3. Tạo migration mới; không đưa database dump hoặc migration cũ vào đây nếu chưa review ownership.
4. Giữ path/body/response frontend đang dùng ở P0. Nếu đổi contract, update frontend trong cùng PR hoặc ghi breaking change rõ ràng.
5. Xóa dependency không dùng thay vì bê cả `shared` module cũ sang.

## Handoff trước merge

PR của mỗi service phải có:

- Link controller source cũ đã chuyển.
- Danh sách endpoint mới hoặc endpoint đã giữ tương thích.
- Flyway migration và rollback note nếu cần.
- Unit/controller test; với Rental/Inventory có test HTTP contract liên service.
- `mvn -pl <module> test` pass.

## Gateway path map

| Downstream | Public route Gateway |
|---|---|
| Identity | `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**`, `/api/v1/sessions/**` |
| Organization-Customer | `/api/v1/organizations/**`, `/api/v1/branches/**`, `/api/v1/employees/**`, `/api/v1/customers/**` |
| Inventory | `/api/v1/inventory/**` |
| Rental | `/api/v1/rental-requests/**`, `/api/v1/quotations/**`, `/api/v1/rental-orders/**`, `/api/v1/rental-contracts/**`, `/api/v1/availability/**`, `/api/v1/equipment/search`, `/api/v1/rental-prices/**`, `/api/v1/discount-codes/**` |
| AI | `/api/v1/ai/**` → AI receives `/api/v1/**` |
