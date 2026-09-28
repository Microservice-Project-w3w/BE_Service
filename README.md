# Backend microservice — việc cần làm và cách chia cho nhóm

`backend_microservice` là bản làm lại. Folder `../backend` là source tham khảo: xem controller, DTO, entity, service, repository và test ở đó để chuyển **nghiệp vụ đang chạy** sang đây. Không copy nguyên module cũ rồi sửa lặt vặt; mỗi người tạo migration, entity, DTO, service và test mới trong module của mình.

## Chia người và phạm vi merge

| Người | Folder được sửa chính | Việc phải hoàn thành | Port / DB |
|---|---|---|---|
| Bạn | `api-gateway`, `services/ai-service` | Gateway route/CORS/health, AI chat/Ollama, review contract | 8080 / 8090 |
| Người 1 | `services/identity-service` | Đăng nhập + user/role/permission/session | 8081 / `identity_db` |
| Người 2 | `services/organization-customer-service` | Organization, branch, employee, customer | 8082 / `organization_customer_db` |
| Người 3 | `services/inventory-service` | Danh mục, thiết bị, availability, reservation | 8083 / `inventory_db` |
| Người 4 | `services/rental-service` | Request → quotation → order → contract | 8084 / `rental_db` |

Không sửa module người khác. Nếu cần thêm endpoint, sửa `docs/SERVICE_OWNERSHIP.md` trong PR rồi nhắn owner Gateway thêm route.

## Thứ tự làm để có demo chạy được

1. Identity: login, `GET /auth/me`, role/scope cơ bản.
2. Organization-Customer: organization, branch, customer, employee branch assignment.
3. Inventory: category, equipment, availability và reservation nội bộ.
4. Rental: request → quotation → approve/reject → convert order → reserve.
5. Contract, user administration, pricing/discount và phần P1/P2 làm sau khi P0 chạy qua Gateway.

Mỗi P0 phải có migration Flyway, controller test và chạy được qua `http://localhost:8080`, không chỉ chạy direct service.

---

## Người 1 — Identity service

Nguồn để xem: `../backend/services/identity-service`.

### P0 — bắt buộc cho demo

Chuyển nghiệp vụ từ `AuthController.java` và phần service/repository/DTO mà controller dùng:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `PUT /api/v1/auth/me`
- `PUT /api/v1/auth/password`

Tối thiểu cần các bảng/user model, password hash, JWT access + refresh session, role, organization scope và branch scope. Frontend đang gọi các endpoint trên nên giữ body/response tương thích source cũ trước khi tự đổi contract.

### P1 — Admin quản lý tài khoản

Chuyển từ `IdentityManagementController.java`:

- Role/permission: list, create, update, delete, gán permission cho role.
- User: list/detail/create, đổi role, đổi scope, lock/unlock, reset password, soft-delete.
- Session: list session user và revoke session.

Các path cần có: `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**`, `/api/v1/sessions/**`.

### P2 — làm sau

Email verification code, verify email, forgot-password/reset-password public flow trong `AuthController`. Không bỏ code cũ; chỉ chưa cần đưa vào demo đầu tiên.

**Definition of done:** test login → refresh → `/me` → logout; token mang role, organizationId và branchIds; migration chạy trên database trống.

---

## Người 2 — Organization-Customer service

Nguồn để xem: `../backend/services/organization-customer-service`.

### P0 — bắt buộc

Chuyển use case từ các controller sau:

| Controller cũ | Chức năng cần chuyển |
|---|---|
| `OrganizationController` | Tạo, list, detail, update, soft-delete organization |
| `BranchController` | CRUD branch theo organization |
| `EmployeeController` | CRUD employee theo organization |
| `EmployeeBranchAssignmentController` | Gán employee vào branch, list assignment, deactivate assignment |
| `CustomerController` | CRUD customer, list/filter, detail, ownership lookup |

Giữ các namespace cũ: `/api/v1/organizations/**`; employee/customer nằm dưới organization theo mapping source cũ.

### P1/P2

- P1: `CustomerGroupController` — CRUD group và add/remove/list members.
- P2: `RestrictedCustomerController` — tạo restriction, list/check/remove restriction.

**Definition of done:** tạo organization → branch → employee → assign branch → customer chạy qua Gateway. Không gọi thẳng `identity_db`; chỉ lưu ID/scope do Identity cấp.

---

## Người 3 — Inventory service

Nguồn để xem: `../backend/services/inventory-service`.

### P0 — bắt buộc để Rental chạy

| Controller cũ | Chức năng cần chuyển |
|---|---|
| `EquipmentCategoryController` | CRUD/active category `/api/v1/inventory/categories` |
| `EquipmentController` | CRUD equipment, filter/list/detail, status, search serial/IMEI/MAC |
| `AvailabilityController` | API nội bộ kiểm tra availability cho Rental |
| `InternalReservationController` | Create/confirm/release reservation cho Rental |
| `EquipmentReservationController` | List/detail reservation để Manager xem |
| `InternalEquipmentQueryController`, `InternalEquipmentStatusController` | Rental/Operations query và update trạng thái nội bộ |

P0 phải quyết định rõ trạng thái thiết bị và rule reserve/release để Rental không giữ chỗ hai lần.

### P1

`BrandController`, `EquipmentTypeController`, `EquipmentModelController`, `WarehouseController`, `StockInController`, `StockOutController`, `StockTransferController`.

### P2

`EquipmentAccessoryController`, `EquipmentImageController`, `EquipmentQrController`, `EquipmentStatusHistoryController`, `EquipmentTransactionController`, `StockAuditController`, `InternalEquipmentCheckoutController`, `InternalEquipmentCheckinController`.

Public path vẫn là `/api/v1/inventory/**`. Path `/internal/**` chỉ cho Rental/Operations service, không đi qua public Gateway trừ khi bạn chủ động mở route mới.

**Definition of done:** admin tạo category/equipment; Rental gọi availability → create reservation → confirm/release; test concurrent reservation tối thiểu cho cùng một equipment.

---

## Người 4 — Rental service

Nguồn để xem: `../backend/services/rental-service`.

### P0 — luồng demo chính

Chuyển từ `RentalWorkflowController.java`:

1. Rental request: create, list, detail, update, cancel.
2. Quotation: create, list, detail, send, update, approve, reject có reason, accept.
3. Convert quotation sang rental order.
4. Rental order: list, detail, reserve, confirm, cancel.
5. Availability/equipment search dùng dữ liệu Inventory qua internal API, không copy bảng equipment qua Rental.

Chuyển từ `ContractController.java` sau khi order ổn:

- Create/list/detail contract.
- Approve/reject có reason/sign/cancel/liquidate.
- Extension và appendix có thể làm sau nhưng giữ DTO/path để frontend không lệch.

### P1/P2

- P1: `PricingController` — rental prices và discount codes.
- P2: `InternalRentalOwnershipController` — ownership check cho service khác.

Public path bắt buộc: `/api/v1/rental-requests/**`, `/api/v1/quotations/**`, `/api/v1/rental-orders/**`, `/api/v1/rental-contracts/**`, `/api/v1/availability/**`, `/api/v1/equipment/search`, `/api/v1/rental-prices/**`, `/api/v1/discount-codes/**`.

**Definition of done:** customer/sales tạo request → sales tạo quotation → manager approve/reject → convert order → Inventory reserve → contract. Cả luồng phải có integration test với mock HTTP Inventory hoặc test container.

---

## Việc của bạn — Gateway và AI

### Gateway

Routes đã khai báo sẵn trong `api-gateway/src/main/resources/application.yml` cho đúng các path P0/P1 ở trên. Khi người khác thêm public path mới, chỉ sửa file này sau khi có controller test downstream. Không biến Gateway thành nơi chứa nghiệp vụ, database hoặc JWT business logic; service đích phải tự authorize.

Test tối thiểu trước merge Gateway:

```bash
mvn -pl api-gateway test
mvn -pl api-gateway spring-boot:run
curl http://localhost:8080/actuator/health
```

### AI service

AI đang có endpoint direct `POST http://localhost:8090/api/v1/chat`, qua Gateway là `POST /api/v1/ai/chat`. Nó gọi Ollama tại `OLLAMA_BASE_URL`; chạy infrastructure AI bằng:

```bash
cd infra
docker compose --profile ai up -d
ollama pull qwen2.5:7b
```

Khi thêm truy vấn dữ liệu nghiệp vụ cho AI, AI service chỉ gọi API public/internal đã được duyệt; không dùng database của Identity/Inventory/Rental trực tiếp.

---

## Cách chạy chung

```bash
cp .env.example .env
cd infra && docker compose up -d
mvn -pl api-gateway spring-boot:run
mvn -pl services/ai-service spring-boot:run
```

Mỗi service có database riêng được tạo bởi `infra/mysql/init.sql`. Migration đặt trong đúng service: `src/main/resources/db/migration`. Khi bắt đầu viết entity, đổi `spring.flyway.enabled` sang `true`; không dùng `ddl-auto=update` trên môi trường chung.
