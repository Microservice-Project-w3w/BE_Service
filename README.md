# Backend microservice — việc cần làm và cách chia cho nhóm

`backend_microservice` là bản làm lại. Folder `../backend` là source tham khảo: xem controller, DTO, entity, service, repository và test ở đó để chuyển **nghiệp vụ đang chạy** sang đây. Không copy nguyên module cũ rồi sửa lặt vặt; mỗi người tạo migration, entity, DTO, service và test mới trong module của mình.

## Chia người và phạm vi merge

| Người | Folder được sửa chính | Việc phải hoàn thành | Port / DB |
|---|---|---|---|
| Phạm Đình Đức Vượng | `api-gateway`, project `E:\equipment-rental-AI` | Gateway route, CORS, health check, AI chat/Ollama và merge các service vào luồng chung | 8080 / 8090 |
| Tô Trung Tuấn | `services/identity-service`; một phần `services/inventory-service` | Identity: đăng nhập, user, role, permission, session. Inventory: danh mục, master data và CRUD thiết bị | 8081 / `identity_db`; 8083 / `inventory_db` |
| Trần Minh Tú | Một phần `services/inventory-service` | Availability, reservation, internal equipment query/status; warehouse, nhập/xuất/chuyển kho và kiểm kê | 8083 / `inventory_db` |
| Bùi Nhật Long | `services/organization-customer-service` | Organization, branch, employee, customer | 8082 / `organization_customer_db` |
| Phạm Quốc Việt | `services/rental-service` | Rental request → quotation → order → contract | 8084 / `rental_db` |

Không sửa module người khác. Nếu cần thêm endpoint, ghi rõ endpoint, request/response và service sở hữu trong mô tả PR rồi nhắn Phạm Đình Đức Vượng thêm route Gateway.

## Thứ tự làm để có demo chạy được

1. Identity: login, `GET /auth/me`, role/scope cơ bản.
2. Organization-Customer: organization, branch, customer, employee branch assignment.
3. Inventory: category, equipment, availability và reservation nội bộ.
4. Rental: request → quotation → approve/reject → convert order → reserve.
5. Contract, user administration, pricing/discount và phần P1/P2 làm sau khi P0 chạy qua Gateway.

Mỗi P0 phải có migration Flyway, controller test và chạy được qua `http://localhost:8080`, không chỉ chạy direct service.

---

## Tô Trung Tuấn — Identity service

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

## Bùi Nhật Long — Organization-Customer service

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

## Tô Trung Tuấn và Trần Minh Tú — Inventory service

Nguồn để xem: `../backend/services/inventory-service`.

Phân chia trong cùng module để tránh sửa trùng file:

- **Tô Trung Tuấn:** `EquipmentCategoryController`, `EquipmentController`, cùng entity/DTO/service/repository cho danh mục, brand, type, model và thiết bị.
- **Trần Minh Tú:** `AvailabilityController`, `InternalReservationController`, `EquipmentReservationController`, các internal query/status endpoint, warehouse và nghiệp vụ nhập/xuất/chuyển kho.

### P0 — bắt buộc để Rental chạy

| Owner | Controller cũ | Chức năng cần chuyển |
|---|---|---|
| Tô Trung Tuấn | `EquipmentCategoryController` | CRUD/active category `/api/v1/inventory/categories` |
| Tô Trung Tuấn | `EquipmentController` | CRUD equipment, filter/list/detail, status, search serial/IMEI/MAC |
| Trần Minh Tú | `AvailabilityController` | API nội bộ kiểm tra availability cho Rental |
| Trần Minh Tú | `InternalReservationController` | Create/confirm/release reservation cho Rental |
| Trần Minh Tú | `EquipmentReservationController` | List/detail reservation để Manager xem |
| Trần Minh Tú | `InternalEquipmentQueryController`, `InternalEquipmentStatusController` | Rental/Operations query và update trạng thái nội bộ |

P0 phải quyết định rõ trạng thái thiết bị và rule reserve/release để Rental không giữ chỗ hai lần.

### P1

- **Tô Trung Tuấn:** `BrandController`, `EquipmentTypeController`, `EquipmentModelController`.
- **Trần Minh Tú:** `WarehouseController`, `StockInController`, `StockOutController`, `StockTransferController`.

### P2

- **Tô Trung Tuấn:** `EquipmentAccessoryController`, `EquipmentImageController`, `EquipmentQrController`, `EquipmentStatusHistoryController`.
- **Trần Minh Tú:** `EquipmentTransactionController`, `StockAuditController`, `InternalEquipmentCheckoutController`, `InternalEquipmentCheckinController`.

Public path vẫn là `/api/v1/inventory/**`. Path `/internal/**` chỉ cho Rental/Operations service, không đi qua public Gateway trừ khi bạn chủ động mở route mới.

**Definition of done:** admin tạo category/equipment; Rental gọi availability → create reservation → confirm/release; test concurrent reservation tối thiểu cho cùng một equipment.

---

## Phạm Quốc Việt — Rental service

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

## Việc của Phạm Đình Đức Vượng — Gateway và AI

### Gateway

Routes đã khai báo sẵn trong `api-gateway/src/main/resources/application.yml` cho đúng các path P0/P1 ở trên. Khi người khác thêm public path mới, chỉ sửa file này sau khi có controller test downstream. Không biến Gateway thành nơi chứa nghiệp vụ, database hoặc JWT business logic; service đích phải tự authorize.

Test tối thiểu trước merge Gateway:

```bash
mvn -pl api-gateway test
mvn -pl api-gateway spring-boot:run
curl http://localhost:8080/actuator/health
```

### AI service

AI chạy từ project riêng `E:\equipment-rental-AI` bằng PowerShell trên Windows; không chạy module Java `services/ai-service` trong repository này vì cả hai đều dùng port `8090`. Gateway chạy trong WSL tự tìm Windows host IP và chuyển `POST /api/v1/ai/chat` tới AI FastAPI.

AI cần Gateway tại `http://localhost:8080` để xác thực token qua Identity và lấy dữ liệu nghiệp vụ. Chỉ role `ADMIN` và `MANAGER` được dùng chat. AI chỉ gọi API qua Gateway, không truy cập trực tiếp database của các service Java.

Trong `E:\equipment-rental-AI\.env` đặt:

```env
APP_HOST=0.0.0.0
APP_PORT=8090
API_GATEWAY_BASE_URL=http://localhost:8080
OLLAMA_BASE_URL=http://127.0.0.1:11434
OLLAMA_MODEL=viet-tutor-frog
```

Chạy AI bằng PowerShell:

```powershell
cd E:\equipment-rental-AI
& .\.venv-win\Scripts\Activate.ps1
python run.py
```

Chạy các service Java bằng `./scripts/run-local-service.sh <service>` để script tự cập nhật `AI_SERVICE_URL` từ default route WSL khi Windows/WSL đổi IP. Không cần Docker Ollama nếu Ollama đã chạy trên Windows và đã có model đã chọn.

Script sẽ tự cài ba module dùng chung (`common-web`, `common-security`, `event-contracts`) vào Maven local repository trước khi chạy service. Lần đầu có thể lâu hơn vì Maven tải dependency và compile.

Không dùng `source .env` trực tiếp để chạy Java service: `DB_URL` có ký tự `&` và Bash có thể diễn giải sai. Luôn dùng script trên để nạp `.env` an toàn.

---

## Cách chạy chung

Các lệnh dưới đây chạy trong WSL/Linux, từ folder `backend_microservice`. Máy cần Java 21, Maven và Docker Compose. Chưa cần bật AI để thử đăng nhập, khách hàng, thiết bị/kho và luồng thuê.

### 1. Chuẩn bị `.env` và MySQL

```bash
cd ~/backend_microservice
# Chỉ copy lần đầu; giữ .env đã cấu hình nếu file đã có.
test -f .env || cp .env.example .env

# Sinh JWT secret, rồi copy kết quả vào JWT_SECRET_BASE64 trong .env.
openssl rand -base64 32
```

Mở `.env` và kiểm tra:

- `JWT_SECRET_BASE64`: thay dòng placeholder bằng secret vừa sinh. Tất cả service trên cùng máy dùng chung file `.env` này.
- `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `DB_PASSWORD`: phải khớp mật khẩu MySQL đang dùng.
- `MYSQL_PORT`: mặc định bản demo là `3307`, tránh trùng MySQL local ở `3306`. Nếu đổi port, sửa cả port trong `DB_URL`.
- Giữ nguyên tên database: `identity_db`, `organization_customer_db`, `inventory_db`, `rental_db`.
- `MAIL_USERNAME`/`MAIL_PASSWORD` có thể để trống khi chỉ thử bằng tài khoản demo, không gửi email.

Không commit `.env` hay mật khẩu SMTP/JWT thật. `.env.example` là mẫu cấu hình để mọi người tự tạo `.env` trên máy mình. Không dùng `source .env`; script chạy service sẽ nạp file này an toàn.

```bash
docker compose --env-file .env -f infra/docker-compose.yml up -d
docker compose --env-file .env -f infra/docker-compose.yml ps
```

Chờ service `mysql` hiện `healthy`. Lệnh trên không bật Ollama/AI vì không dùng `--profile ai`.

Schema bốn database nằm trong `infra/mysql/init/01-core-service-schema.sql`. MySQL tự nạp schema khi tạo volume trống **lần đầu**, không tự nạp lại khi `git pull` hoặc restart container. Seed bên dưới chỉ nạp dữ liệu, không thay thế việc tạo schema. Nếu đang dùng volume cũ mà báo thiếu bảng/cột, kiểm tra schema/migration trước; **không chạy `docker compose down -v` để chữa lỗi** vì sẽ xóa dữ liệu.

## Dữ liệu demo

### 2. Khởi động Identity trước

Trong terminal thứ nhất:

```bash
cd ~/backend_microservice
bash scripts/run-local-service.sh identity-service
```

Chờ log `Started IdentityServiceApplication`, giữ terminal này chạy. Identity tự nạp role/permission từ `services/identity-service/src/main/resources/security/`; script cũng tự cài các module Maven dùng chung, không cần build từng thư viện bằng tay.

### 3. Nạp seed

Mở terminal thứ hai:

```bash
cd ~/backend_microservice
bash scripts/load-demo-seed.sh
```

Thành công sẽ hiện `Demo data loaded. Demo account password: Demo@123`.

Script đọc cấu hình MySQL từ `.env`, dùng container `mysql` trong Compose và nạp đúng thứ tự sau; không cần cài thêm MySQL client trên WSL hoặc chạy từng file SQL bằng tay:

| File trong `infra/mysql/seed/` | Dữ liệu |
|---|---|
| `01-organization-customer-demo.sql` | Tổ chức, hai chi nhánh, nhân viên, hai khách hàng |
| `02-identity-demo.sql` | Năm tài khoản demo và phạm vi chi nhánh |
| `03-organization-customer-assignments-demo.sql` | Liên kết user với nhân viên/khách hàng và phân công chi nhánh |
| `04-inventory-demo.sql` | Danh mục, brand/type/model, kho, bốn thiết bị, reservation và phiếu kho/kiểm kê |
| `05-rental-demo.sql` | Giá thuê, mã giảm giá, yêu cầu thuê, báo giá, đơn thuê, hợp đồng và phụ lục |

**Chỉ dùng seed trên database dev/demo.** Có thể chạy lại, nhưng mỗi lần chạy sẽ cập nhật dữ liệu mẫu: mật khẩu về `Demo@123`, trạng thái về trạng thái seed và thời gian thuê/giữ chỗ được làm mới. Vì vậy các thao tác đã thử trên bản ghi demo có thể bị đặt lại. Script không xóa/truncate database; nếu cần giữ trạng thái demo đang làm thì không nạp lại. Nên seed trước khi bật Inventory/Rental, tránh thao tác hoặc job giữ chỗ chạy đồng thời.

### 4. Bật các service còn lại và đăng nhập frontend

Giữ Identity chạy. Mở thêm bốn terminal tại folder `backend_microservice`, **mỗi terminal chạy một lệnh**, không paste cả khối vào cùng một terminal:

```bash
bash scripts/run-local-service.sh organization-customer-service
bash scripts/run-local-service.sh inventory-service
bash scripts/run-local-service.sh rental-service
bash scripts/run-local-service.sh api-gateway
```

Chờ từng service báo `Started ...Application`, sau đó mở frontend đã chạy bằng `npm run dev` (mặc định `http://localhost:5173`). Frontend gọi Gateway ở `http://localhost:8080`, không gọi trực tiếp port của từng service. Sau khi đổi role/quyền hoặc seed lại, đăng xuất và đăng nhập lại để nhận token mới.

Mật khẩu chung cho cả năm tài khoản: **`Demo@123`**.

| Đăng nhập | Vai trò | Dùng để demo |
|---|---|---|
| `rentai.demo.admin@gmail.com` | Admin | tài khoản, tổ chức, chi nhánh, danh mục |
| `rentai.demo.manager@gmail.com` | Manager | duyệt báo giá, đơn thuê, hợp đồng, AI chat |
| `rentai.demo.sales@gmail.com` | Sales | khách hàng, yêu cầu thuê và báo giá |
| `rentai.demo.operations@gmail.com` | Operations | thiết bị, kho, nhập/xuất/chuyển/kiểm kê |
| `rentai.demo.customer@gmail.com` | Customer | dữ liệu khách hàng mẫu (customer portal đang ẩn ở MVP) |

Seed tạo một tổ chức, hai chi nhánh, hai khách hàng, danh mục/kho/bốn thiết bị với các trạng thái sẵn sàng–giữ chỗ–bảo dưỡng, cùng luồng thuê có yêu cầu mới, báo giá chờ duyệt, đơn đã giữ chỗ, hợp đồng chờ duyệt và hợp đồng đang hiệu lực.

Luồng thử đầy đủ: Operations chuẩn bị thiết bị → Sales tạo yêu cầu/báo giá → Manager duyệt → Sales ghi nhận khách chấp nhận và chuyển thành đơn → Manager giữ chỗ, xác nhận giữ chỗ → Sales tạo hợp đồng → Manager duyệt → Sales ghi nhận đã ký. Gia hạn làm trên hợp đồng qua phụ lục duyệt/ký, không gia hạn trực tiếp đơn thuê.

### Nếu seed chưa chạy được

- `Missing .../.env`: tạo `.env` từ `.env.example`, điền cấu hình như bước 1.
- `Identity role seed is missing`: khởi động Identity và chờ startup hoàn tất trước khi chạy lại seed.
- `service "mysql" is not running`: chạy Compose ở bước 1, chờ MySQL healthy.
- `Access denied`: kiểm tra `.env` có đúng mật khẩu của **volume MySQL hiện có** không. Đổi mật khẩu trong `.env` không tự đổi mật khẩu database trong volume cũ.
- `Table ... doesn't exist` hoặc `Unknown column`: kiểm tra schema/migration của database đang dùng; không xóa volume hay nạp seed liên tục để sửa lỗi schema.

Khi đưa lên Git, giữ đủ `.env.example`, `infra/mysql/init/`, `infra/mysql/seed/`, `scripts/load-demo-seed.sh` và `scripts/run-local-service.sh`. Các file này đã có trong repo; người kéo code chỉ cần cấu hình `.env` riêng rồi làm theo thứ tự trên. AI là phần tùy chọn, cách chạy riêng ở mục AI service phía trên.
