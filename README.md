# RentAI — Equipment Rental Backend

Backend cho hệ thống quản lý cho thuê thiết bị, tổ chức theo bốn dịch vụ nghiệp vụ độc lập và một API Gateway. Dự án hỗ trợ quy trình yêu cầu thuê → báo giá → giữ chỗ → hợp đồng → bàn giao → thu tiền → nhận trả, cùng trợ lý AI tra cứu dữ liệu theo quyền.

Repository này có thể clone, chạy và kiểm thử độc lập bằng REST API với curl/Postman, không yêu cầu frontend hoặc AI. Frontend và AI là các repository tích hợp tùy chọn.

Đây là dự án nhóm phục vụ học tập và trình diễn nghiệp vụ. Các chức năng tài chính, bàn giao, bảo trì và ký xác nhận được triển khai ở mức demo, không phải hệ thống thanh toán hay chữ ký số thương mại.

[Frontend](https://github.com/Microservice-Project-w3w/FE_service) · [AI Assistant](https://github.com/Microservice-Project-w3w/Equipment-Rental-AI)

[Kiến trúc](#kiến-trúc) · [Chạy local](#chạy-local-từ-máy-mới) · [Kịch bản demo](#kịch-bản-trình-diễn) · [AI](#trợ-lý-ai-tùy-chọn) · [Kiểm thử](#api-và-kiểm-thử) · [Nhóm thực hiện](#nhóm-thực-hiện)

## Kiến trúc

```text
curl / Postman / Frontend (tùy chọn)
    │
API Gateway :8080
    ├── Identity :8081              → identity_db
    ├── Organization-Customer :8082 → organization_customer_db
    ├── Inventory :8083             → inventory_db
    ├── Rental :8084                → rental_db
    └── AI Assistant :8090          → Ollama (tùy chọn)

Rental ── internal REST API ──→ Inventory
AI ── REST API qua Gateway ──→ các dịch vụ theo quyền người hỏi
```

Bốn dịch vụ dùng các database riêng trên cùng MySQL instance trong môi trường local. Gateway định tuyến, xử lý CORS, chuyển tiếp mã truy vết và chuẩn hóa một số lỗi kết nối; quyền truy cập và nghiệp vụ được kiểm tra tại service đích. Đường dẫn `/internal/**` của Inventory không được mở qua Gateway công khai.

AI được chạy từ repository Python riêng. Module Java `services/ai-service` còn trong source tham khảo, không chạy đồng thời với AI Python vì cùng sử dụng cổng `8090`.

### Điểm kỹ thuật

- **Ranh giới dịch vụ:** mỗi service sở hữu database và nghiệp vụ riêng; Rental trao đổi với Inventory qua REST API thay vì đọc chéo bảng.
- **Bảo mật nhiều lớp:** Gateway là điểm vào chung; service đích kiểm tra JWT, permission, organization/branch scope và quyền sở hữu tài liệu của Customer.
- **Tính nhất quán:** transaction và khóa bản ghi cho các bước nghiệp vụ quan trọng; nhật ký bù trừ/thử lại cho thao tác Rental–Inventory, `requestKey` chống ghi trùng.
- **Khả năng truy vết:** chuyển tiếp `X-Trace-Id`, health endpoint và lịch sử giao dịch/biên bản; giữ riêng tiền thuê với tiền cọc.
- **Kiểm thử:** unit/controller test cho validation, phân quyền, chuyển trạng thái và lỗi tích hợp; workflow test với API thật được cung cấp ở repository frontend.

Có thể bắt đầu đọc source tại [cấu hình Gateway](api-gateway/src/main/resources/application.yml), [bộ kiểm tra session dùng chung](shared/common-security/src/main/java/com/equipmentrental/common/security/LiveSessionValidator.java), [InventoryClient](services/rental-service/src/main/java/com/equipmentrental/rental/client/InventoryClient.java) và [nhật ký đồng bộ](services/rental-service/src/main/java/com/equipmentrental/rental/service/DemoSyncJournal.java).

## Công nghệ

- Java 21, Spring Boot 3.4, Spring Cloud Gateway và Maven multi-module.
- Spring Security, JWT, phân quyền theo vai trò/quyền và phạm vi tổ chức/chi nhánh.
- Spring Data JPA, Hibernate, MySQL 8; schema, migration và seed SQL được lưu trong repository.
- Docker Compose cho MySQL; các ứng dụng Java chạy riêng khi phát triển local.
- JUnit, Mockito và Spring Test; kiểm thử workflow/API thật từ repository frontend.
- AI tùy chọn: Python 3.12+, FastAPI, Ollama và công cụ tra cứu REST API.

## Chức năng

| Thành phần            | Chức năng chính                                                                                                      | Cổng   |
| --------------------- | -------------------------------------------------------------------------------------------------------------------- | ------ |
| API Gateway           | Định tuyến API, CORS, health endpoint, `X-Trace-Id`, phản hồi lỗi downstream 503/504.                                | `8080` |
| Identity              | Đăng ký/đăng nhập, hồ sơ, mật khẩu, user, role, permission, scope và quản lý phiên đăng nhập.                        | `8081` |
| Organization-Customer | Tổ chức, chi nhánh, nhân viên, phân công/chuyển chi nhánh, khách hàng và liên kết tài khoản Customer.                | `8082` |
| Inventory             | Danh mục, brand/type/model, thiết bị, kho, nhập/xuất/chuyển/kiểm kê, lịch trống, giữ chỗ, giao/nhận và bảo trì demo. | `8083` |
| Rental                | Yêu cầu thuê, báo giá, đơn thuê, hợp đồng, phụ lục/gia hạn, giá thuê/mã giảm giá và chứng từ tài chính demo.         | `8084` |
| AI Assistant          | Tra cứu dữ liệu theo quyền cho Admin/Manager, diễn đạt kết quả và hỗ trợ chế độ nhẹ không cần model.                 | `8090` |

### Các ràng buộc nghiệp vụ

- Customer chỉ truy cập hồ sơ thuê thuộc tài khoản đã liên kết; đổi ID trên URL không thay đổi quyền sở hữu.
- Báo giá hết hạn không được duyệt/chấp nhận/chuyển đơn. Số tiền không âm; giảm giá không vượt giá trị đơn.
- Giữ chỗ phải đủ đúng số lượng từng loại thiết bị, kiểm tra thời gian trùng và trạng thái khả dụng. Các bước quan trọng có khóa bản ghi để kiểm soát thao tác đồng thời.
- Hợp đồng bị từ chối được sửa/gửi duyệt lại với cùng mã/ID. Gia hạn đi qua phụ lục hợp đồng, không cập nhật trực tiếp từ màn hình đơn thuê.
- Thu tiền thuê và cọc được ghi riêng. Không thu vượt công nợ/cọc yêu cầu; không hoàn/khấu trừ vượt số cọc đang giữ.
- COD chỉ được ghi nhận đã thu sau bàn giao. Thanh lý cần có biên bản trả, tất toán tiền thuê và xử lý hết cọc.
- Thiết bị trả về POOR/DAMAGED chuyển sang INSPECTION; thiết bị đang cho thuê/bảo trì không được xem là sẵn sàng.
- Rental lưu ý định đồng bộ trước khi đổi Inventory. Rollback dùng bù trừ; thao tác chưa xác định kết quả được lưu để người có quyền thử lại, chống ghi trùng bằng `requestKey`.
- Sau logout, reset mật khẩu hoặc thu hồi phiên, token bị từ chối ở request bảo vệ kế tiếp, kể cả gọi trực tiếp service. Identity phải hoạt động để các service xác minh session.

## Chạy local từ máy mới

Các lệnh dưới đây dành cho Bash trên Linux/WSL. Cần Java 21, Maven, Docker với Compose v2, Git, OpenSSL và curl để kiểm tra API. Không cần Node.js, Python hoặc Ollama để chạy backend lõi.

### 1. Clone backend

Chạy tại thư mục muốn lưu dự án:

```bash
git clone https://github.com/Microservice-Project-w3w/BE_Service.git backend_microservice
cd backend_microservice
cp .env.example .env
openssl rand -base64 32
```

Điền kết quả sinh secret vào `JWT_SECRET_BASE64` trong `.env`. Chỉ sao chép `.env` ở lần thiết lập đầu tiên, không ghi đè cấu hình riêng khi cập nhật code.

| Cấu hình                                               | Cần kiểm tra                                                                           |
| ------------------------------------------------------ | -------------------------------------------------------------------------------------- |
| `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `DB_PASSWORD` | Dùng mật khẩu local của bạn; với cấu hình root mặc định, ba giá trị phải khớp nhau.    |
| `MYSQL_HOST`, `MYSQL_PORT`                             | Mặc định `localhost:3307`, tránh trùng MySQL local ở `3306`.                           |
| `DB_URL`                                               | JDBC URL của Rental; cổng phải khớp `MYSQL_PORT`, database vẫn là `rental_db`.         |
| `JWT_SECRET_BASE64`, `JWT_ISSUER`                      | Dùng chung giữa các service trên cùng môi trường.                                      |
| `FRONTEND_URL`                                         | Origin frontend được CORS cho phép, mặc định `http://localhost:5173`.                  |
| `AI_SERVICE_RUNS_ON_WINDOWS`                           | `true` khi AI chạy Windows còn Gateway ở WSL; `false` khi không dùng cách kết nối này. |
| `AI_SERVICE_URL`                                       | Nếu AI chạy cùng Linux/WSL, đặt `http://localhost:8090` và tắt cờ Windows.             |
| `MAIL_USERNAME`, `MAIL_PASSWORD`                       | Có thể để trống khi dùng tài khoản seed và không thử gửi email.                        |

Giữ nguyên tên `identity_db`, `organization_customer_db`, `inventory_db`, `rental_db`. Không commit `.env` hoặc secret thật. Không dùng `source .env`: JDBC URL có ký tự `&`; script chạy local đã nạp cấu hình an toàn.

### 2. Khởi động MySQL

Từ thư mục backend:

```bash
docker compose --env-file .env -f infra/docker-compose.yml up -d mysql
docker compose --env-file .env -f infra/docker-compose.yml ps
```

Chờ `mysql` hiển thị `healthy`. Lệnh chỉ chạy MySQL, không tải image Ollama hoặc model AI. MySQL publish cổng `3307` theo `.env`, cổng bên trong container vẫn là `3306`.

Schema trong `infra/mysql/init/` được nạp khi tạo volume trống lần đầu. Restart container hoặc `git pull` không tự chạy lại SQL khởi tạo. Không dùng `docker compose down -v` để sửa lỗi schema vì sẽ xóa dữ liệu.

### 3. Khởi động Identity và nạp seed

Terminal thứ nhất, tại thư mục backend:

```bash
bash scripts/run-local-service.sh identity-service
```

Chờ `Started IdentityServiceApplication`, giữ terminal chạy. Identity nạp role/permission từ `services/identity-service/src/main/resources/security/`.

Terminal thứ hai, cũng tại thư mục backend:

```bash
bash scripts/load-demo-seed.sh
```

Script dùng MySQL trong Compose, không yêu cầu cài MySQL client riêng. Nó nạp tổ chức, hai chi nhánh, khách hàng/nhân viên, năm tài khoản, thiết bị/kho và hồ sơ thuê mẫu; đồng thời chuẩn bị Customer portal và các bảng chứng từ/bảo trì demo.

Chỉ seed database dev/demo. Chạy lại seed có thể đặt lại mật khẩu, trạng thái và thời gian của bản ghi mẫu dù không truncate database. Seed trước khi bật Inventory/Rental; không nạp lại nếu cần giữ nguyên phiên demo đang thực hiện.

### 4. Khởi động các service còn lại

Mỗi dòng dưới đây chạy trong **một terminal riêng**, tại thư mục backend:

```bash
bash scripts/run-local-service.sh organization-customer-service
bash scripts/run-local-service.sh inventory-service
bash scripts/run-local-service.sh rental-service
bash scripts/run-local-service.sh api-gateway
```

Script tự cài `common-web`, `common-security`, `event-contracts` vào Maven local repository trước khi chạy module đã chọn. Lần đầu cần tải dependency; không phải tự build từng thư viện chung. Chờ từng service báo startup thành công.

### 5. Kiểm tra API, không cần frontend

Mở terminal mới sau khi các service startup thành công:

```bash
curl --connect-timeout 3 --max-time 5 http://localhost:8080/actuator/health
curl --connect-timeout 3 --max-time 5 http://localhost:8080/gateway/health/identity
curl --connect-timeout 3 --max-time 5 http://localhost:8080/gateway/health/organization-customer
curl --connect-timeout 3 --max-time 5 http://localhost:8080/gateway/health/inventory
curl --connect-timeout 3 --max-time 5 http://localhost:8080/gateway/health/rental
```

Đăng nhập tài khoản seed qua Gateway:

```bash
curl --max-time 15 -i -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"rentai.demo.admin@gmail.com","password":"Demo@123"}'
```

Copy `data.accessToken` từ JSON trả về rồi dùng cho các request được bảo vệ:

```bash
ACCESS_TOKEN='PASTE_ACCESS_TOKEN_HERE'
curl --max-time 15 http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
curl --max-time 15 'http://localhost:8080/api/v1/rental-orders?organizationId=1&branchId=1' \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Với Postman: đặt base URL `http://localhost:8080`, gửi request login ở trên, sau đó chọn **Authorization → Bearer Token** và nhập `data.accessToken`. Body của request ghi dữ liệu dùng **raw → JSON**. ID `1` trong ví dụ thuộc dữ liệu seed; thay bằng ID thực tế nếu sử dụng dữ liệu khác. Không đưa access/refresh token thật vào Git hoặc ảnh minh họa công khai.

### Giao diện web (tùy chọn)

Nếu cần trình diễn bằng giao diện, clone [frontend](https://github.com/Microservice-Project-w3w/FE_service) và làm theo README của repository đó; cần thêm Node.js 24/npm. Frontend mặc định chạy tại `http://localhost:5173` và gọi Gateway `8080`. Bước này không bắt buộc để chạy hay kiểm thử các API backend.

### Tài khoản demo

Mật khẩu chung: `Demo@123`. Chỉ dùng trong môi trường local/demo.

| Vai trò    | Email                              | Phạm vi thử nghiệm                                            |
| ---------- | ---------------------------------- | ------------------------------------------------------------- |
| Admin      | `rentai.demo.admin@gmail.com`      | Tài khoản, tổ chức, chi nhánh, danh mục, liên kết Customer.   |
| Manager    | `rentai.demo.manager@gmail.com`    | Duyệt, giữ chỗ/xác nhận đơn, chứng từ, bảo trì và AI.         |
| Sales      | `rentai.demo.sales@gmail.com`      | Khách hàng, yêu cầu thuê, báo giá và hợp đồng.                |
| Operations | `rentai.demo.operations@gmail.com` | Thiết bị/kho, giao/nhận và bảo trì.                           |
| Customer   | `rentai.demo.customer@gmail.com`   | Danh mục, lịch trống và quy trình thuê của hồ sơ đã liên kết. |

Seed không tạo sẵn Accountant. Sau khi đổi role/scope hoặc nạp lại seed, đăng xuất và đăng nhập lại để nhận token mới.

## Kịch bản trình diễn

Có thể thực hiện bằng curl/Postman. Chọn token của vai trò tương ứng; HTTP method và body được định nghĩa tại controller/DTO trong từng service.

1. Operations chuẩn bị thiết bị khả dụng trong kho.
2. Customer hoặc Sales tạo yêu cầu thuê; Sales lập/gửi báo giá.
3. Manager duyệt; Customer chấp nhận hoặc Sales ghi nhận chấp nhận, rồi chuyển thành đơn.
4. Manager giữ chỗ và xác nhận đơn; Sales tạo hợp đồng, Manager duyệt.
5. Qua các API con của `/api/v1/rental-orders/{id}`, ghi nhận ký bằng `/demo-signature`, phát hành `/invoice` và thu cọc qua `/transactions/DEPOSIT_COLLECT`.
6. Operations bàn giao qua `/handovers/DELIVERED`; Admin/Manager ghi nhận tiền thuê COD bằng `/transactions/PAYMENT`, có thể thu một phần rồi thu nốt.
7. Operations nhận trả qua `/handovers/RETURNED`; Admin/Manager hoàn/khấu trừ cọc và gọi `/demo-liquidate` sau tất toán.
8. Nếu thiết bị cần sửa, tạo phiếu qua `/api/v1/inventory/maintenance`, bắt đầu và hoàn thành bảo trì.

Customer mới chưa có liên kết tự động: Admin cấp tổ chức/chi nhánh, Sales tạo hồ sơ khách hàng; Admin liên kết bằng `PUT /api/v1/organizations/{organizationId}/customers/{customerId}/portal-account`, body `{"userId": <id>}`. Backend kiểm tra tài khoản CUSTOMER đang ACTIVE, cùng tổ chức và có chi nhánh tương ứng.

### Cập nhật máy đã có dữ liệu

Không cần nạp lại seed để thêm các chức năng demo:

```bash
bash scripts/enable-customer-portal-demo.sh
bash scripts/enable-demo-operations.sh
```

Sau đó khởi động lại các service và đăng nhập lại. Bước thứ hai chỉ tạo thêm năm bảng `demo_*` trong database cũ. Với schema cũ khác nữa, kiểm tra migration phù hợp trước khi chạy; hai script này không thay thế mọi bước nâng cấp schema.

## Trợ lý AI (tùy chọn)

AI chạy từ [Equipment-Rental-AI](https://github.com/Microservice-Project-w3w/Equipment-Rental-AI). Chỉ ADMIN/MANAGER được chat. Công cụ ưu tiên gọi REST API qua Gateway bằng token người hỏi, giới hạn theo permission, tổ chức và chi nhánh; không tự tạo/duyệt/hủy dữ liệu nghiệp vụ.

Ví dụ thiết lập trên Windows, sau khi đã cài Python 3.12; có thể thay đường dẫn bằng thư mục khác:

```powershell
git clone https://github.com/Microservice-Project-w3w/Equipment-Rental-AI.git E:\equipment-rental-AI
cd E:\equipment-rental-AI
py -3.12 -m venv .venv-win
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
& .\.venv-win\Scripts\Activate.ps1
python -m pip install -e ".[test]"
Copy-Item .env.example .env
```

Chỉnh `.env` của AI trước khi chạy; đây là file riêng, không phải `.env` backend:

```dotenv
APP_HOST=0.0.0.0
APP_PORT=8090
API_GATEWAY_BASE_URL=http://localhost:8080
CHAT_MODE=light
CHAT_ENABLED_SERVICES=inventory,rental,organization-customer
```

```powershell
python run.py
```

`light` dùng các mẫu câu tra cứu, không cần Ollama. Để chat qua model, đổi `CHAT_MODE=natural`, đặt `OLLAMA_BASE_URL` và `OLLAMA_MODEL` đúng model đã có, ví dụ `viet-tutor-frog`. Model tùy chỉnh không nằm trong repository và máy mới có thể chưa có; kiểm tra bằng `ollama list`, không mặc định tải model khi chỉ chạy backend.

Nếu AI chạy Windows và Gateway chạy WSL, bật `AI_SERVICE_RUNS_ON_WINDOWS=true` ở backend rồi khởi động Gateway bằng script. Script tự tìm Windows host qua default route WSL; có thể đặt `WSL_WINDOWS_HOST` nếu cần. Đảm bảo Windows Firewall cho phép kết nối từ WSL đến port `8090`, chỉ trong phạm vi mạng phát triển cần thiết.

Kiểm tra từ WSL:

```bash
curl --connect-timeout 3 --max-time 5 http://localhost:8080/gateway/health/ai
```

Tra cứu API thật cần service chứa dữ liệu tương ứng: tồn kho cần Inventory; báo giá/hợp đồng cần Rental; khách hàng cần Organization-Customer; lịch trống cần Rental + Inventory. Gateway, Identity và AI luôn cần cho chat; MySQL phục vụ dữ liệu.

Máy ít RAM có thể bật DB fallback: khi API nghiệp vụ mất kết nối/timeout/502/503/504, AI đọc các view bằng tài khoản riêng chỉ có SELECT. Không fallback cho lỗi quyền hay khi Identity không hoạt động. Migration ở `infra/mysql/migrations/20261011_ai_readonly_views.sql`; thiết lập reader bằng hướng dẫn trong README AI, không cấp root hoặc quyền toàn database cho process chat.

AI hiện tra cứu tồn kho/trạng thái thiết bị, lịch trống, báo giá/hợp đồng, kế hoạch nhận/trả dự kiến và khách hàng. Chưa nối các bảng hóa đơn/cọc, biên bản và phiếu bảo trì mới; không suy ra đã thu tiền hay đã giao/nhận từ giá trị hợp đồng hoặc ngày thuê.

## API và kiểm thử

Client sử dụng base URL `http://localhost:8080`. Các namespace chính:

| Nhóm                 | Đường dẫn                                                                                                                                                                   |
| -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Xác thực và quản trị | `/api/v1/auth`, `/api/v1/users`, `/api/v1/roles`, `/api/v1/permissions`, `/api/v1/sessions`                                                                                 |
| Tổ chức/khách hàng   | `/api/v1/organizations/**`                                                                                                                                                  |
| Thiết bị/kho/bảo trì | `/api/v1/inventory/**`                                                                                                                                                      |
| Quy trình thuê       | `/api/v1/rental-requests`, `/api/v1/quotations`, `/api/v1/rental-orders`, `/api/v1/rental-contracts`                                                                        |
| Tra cứu và giá       | `/api/v1/availability`, `/api/v1/equipment/search`, `/api/v1/rental-prices`, `/api/v1/discount-codes`                                                                       |
| Chứng từ demo        | `/api/v1/rental-orders/{id}/demo-account`, `/invoice`, `/transactions/{kind}`, `/handovers/{kind}`, `/demo-signature`, `/demo-liquidate` (các path con cùng thuộc đơn thuê) |
| Đồng bộ lỗi          | `/api/v1/rental-orders/{id}/sync-tasks` và `/{taskId}/retry` dưới path đó                                                                                                   |
| AI                   | `/api/v1/ai/chat`, `/api/v1/ai/capabilities`                                                                                                                                |

Request nghiệp vụ dùng `Authorization: Bearer <accessToken>`. Xem request/response và HTTP method chính xác trong controller của từng service. `GET /actuator/health` kiểm tra Gateway; `/gateway/health/{identity|organization-customer|inventory|rental|ai}` kiểm tra từng downstream, không thay thế kiểm thử nghiệp vụ.

Kiểm thử các module Java và thư viện chung:

```bash
mvn -pl api-gateway,services/identity-service,services/organization-customer-service,services/inventory-service,services/rental-service -am clean test
```

Lệnh Maven trên kiểm thử backend mà không cần clone/chạy frontend. Các bộ tích hợp bổ sung được lưu tại repository frontend; nếu muốn chạy chúng, cài Node.js/npm và dependency ở repository đó, đồng thời bật MySQL, bốn service và Gateway:

```bash
npm run test:live
npm run test:workflow
npm run test:customer
npm run test:demo
```

Các bộ workflow tạo dữ liệu riêng và có thể giữ chứng từ làm lịch sử; không chạy trên database production. `python -m pytest -q` trong dự án AI chạy test mặc định với backend/model giả lập, không tải model; test DB/runtime thật là opt-in theo README AI.

Không chạy `clean/package` ghi đè JAR hoặc class đang được process khác sử dụng; dừng service cần build rồi khởi động lại sau build. Máy ít RAM nên kiểm thử nghiệp vụ trước, sau đó dừng các process Java do mình mở rồi kiểm thử AI riêng.

## Cấu trúc repository

```text
api-gateway/       # Định tuyến, CORS, health, trace và lỗi kết nối
services/
├── identity-service/
├── organization-customer-service/
├── inventory-service/
├── rental-service/
└── ai-service/    # Module Java tham khảo; runtime AI ở repository Python
shared/
├── common-web/
├── common-security/
└── event-contracts/
infra/mysql/       # Schema khởi tạo, migration và seed demo
scripts/           # Chạy local, seed và cập nhật các bảng demo
```

Thay đổi API cần cập nhật DTO, kiểm thử, client frontend và Gateway nếu mở namespace mới. Service nghiệp vụ không đọc trực tiếp database của nhau; DB fallback AI là tùy chọn riêng, chỉ đọc các view được cấp quyền.

## Nhóm thực hiện

| Thành viên          | Phạm vi backend                                                                                 |
| ------------------- | ----------------------------------------------------------------------------------------------- |
| Phạm Đình Đức Vượng | API Gateway, CORS, health check, trợ lý AI và tích hợp các dịch vụ.                             |
| Tô Trung Tuấn       | Xác thực, tài khoản, vai trò/quyền, phiên đăng nhập; danh mục, dữ liệu nền và CRUD thiết bị.    |
| Trần Minh Tú        | Lịch trống, giữ chỗ, truy vấn/trạng thái thiết bị nội bộ; kho, nhập/xuất/chuyển kho và kiểm kê. |
| Bùi Nhật Long       | Tổ chức, chi nhánh, nhân viên, khách hàng và phân công chi nhánh.                               |
| Phạm Quốc Việt      | Yêu cầu thuê, báo giá, đơn thuê, hợp đồng, giá thuê và mã giảm giá.                             |

## Xử lý lỗi thường gặp

| Hiện tượng                                             | Kiểm tra                                                                                                                  |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------- |
| `Port ... already in use`                              | Xem process bằng `ss -ltnp`; dừng đúng service cũ hoặc đổi port và URL downstream tương ứng.                              |
| Thiếu `common-web`/`common-security`/`event-contracts` | Chạy bằng `scripts/run-local-service.sh` để cài thư viện chung trước.                                                     |
| `Identity role seed is missing`                        | Khởi động Identity, chờ startup hoàn tất rồi nạp seed.                                                                    |
| MySQL `Access denied`                                  | Mật khẩu phải khớp volume hiện có; sửa `.env` không tự đổi mật khẩu trong database.                                       |
| Thiếu bảng/cột                                         | Kiểm tra schema/migration; không xóa volume hoặc seed liên tục để chữa lỗi schema.                                        |
| Frontend timeout/503                                   | Kiểm tra Gateway và service đích, MySQL, cấu hình URL/port; đối chiếu `X-Trace-Id`.                                       |
| HTTP 401 sau logout/đổi mật khẩu                       | Đăng nhập lại; xác nhận Identity hoạt động và JWT issuer/secret thống nhất.                                               |
| Customer chưa dùng được portal                         | Admin kiểm tra scope và liên kết tài khoản với hồ sơ khách hàng.                                                          |
| WSL không gọi được AI Windows                          | Kiểm tra host IP, bind `0.0.0.0`, port `8090` và firewall; localhost Windows thành công chưa chứng minh WSL kết nối được. |

## Giới hạn hiện tại

- Thanh toán COD/tiền mặt/chuyển khoản được nhân viên ghi nhận thủ công; chưa tích hợp ngân hàng, hãng vận chuyển hoặc hóa đơn thuế.
- Ký xác nhận lưu họ tên, người thực hiện, thời gian và SHA-256 nội dung; không xác minh bằng chứng thư số/OTP/dịch vụ ký bên ngoài.
- Bảo trì bắt đầu/kết thúc thủ công; giao/nhận theo cả lô với tình trạng chung và trả về kho ban đầu.
- Hóa đơn chụp số tiền lúc phát hành; chưa tự lập hóa đơn phí gia hạn/thuế/phạt hoặc sửa/xóa giao dịch đã ghi. Tiền cọc không tự bù vào tiền thuê/doanh thu.
- Đồng bộ lỗi có nhật ký bền vững và retry bằng người dùng có quyền; chưa phải saga/outbox hoàn chỉnh hay cơ chế retry nền phân tán. Không lưu bearer token để retry.
- Kiểm tra session online làm các request bảo vệ phụ thuộc Identity và tăng độ trễ; request đã qua xác thực không bị hủy giữa chừng khi thu hồi.
- AI chỉ hỗ trợ tra cứu, chất lượng diễn đạt phụ thuộc chế độ/model; fallback chỉ đọc không thay thế nghiệp vụ của service.
- Dự án chưa được xác nhận sẵn sàng production. Cần hardening cấu hình, bảo vệ secrets, HTTPS, quan sát vận hành và kiểm thử tải trước khi triển khai thực tế.
