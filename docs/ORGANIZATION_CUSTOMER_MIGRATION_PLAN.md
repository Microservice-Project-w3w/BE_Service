# KẾ HOẠCH MIGRATION ORGANIZATION–CUSTOMER SERVICE

Ngày khảo sát: 2026-09-28 (Asia/Saigon)

## 1. Thông tin dự án và trạng thái

| Mục | Kết quả |
|---|---|
| SOURCE_PATH | `C:\Users\viet\Downloads\backend-microservice-organization-customer-service` |
| SOURCE service | `services/organization-customer-service/` |
| TARGET_PATH | `E:\BE_Service` |
| TARGET_REMOTE | `origin = https://github.com/Microservice-Project-w3w/BE_Service.git` (fetch/push URL) |
| CURRENT_BRANCH | `organization-customer-service` (local branch tracking `origin/main` vì remote branch cùng tên chưa tồn tại) |
| WORKING_TREE trước tạo báo cáo | Clean: `## organization-customer-service...origin/main` |
| MAIN_MERGE_BASE | `e47dc5041de1240f026aec860f576fe995fc4aa0` (trùng HEAD và `origin/main`) |
| TOTAL_APIS | 36 API nghiệp vụ; thêm 1 health endpoint ngoài phạm vi |
| DEPENDENCIES | Spring Web, Data JPA, Validation, MySQL runtime, Lombok; các service phụ thuộc nội bộ theo Organization → Branch/Employee → Assignment/Customer → Group/Restriction |
| FOUNDATION_COMMIT_REQUIRED | **YES** — cần POM/Lombok, exception, enum/entity, schema migration và nền test dùng chung; đề xuất riêng, chỉ làm sau khi được duyệt |

Source được giải nén từ ZIP và **không có `.git`**, do đó các lệnh `git status`, `git remote`, `git branch`, `git log` tại SOURCE đều trả về `fatal: not a git repository`. Không có lịch sử nguồn để rewrite hay giả mạo. Không file SOURCE nào bị sửa.

## 2. Quá trình thực hiện

1. Chạy `pwd` tại workspace và xác định đường dẫn thực tế như trên.
2. Chạy `git status --short --branch`, `git remote -v`, `git branch -a`, `git log -5 --oneline` ở SOURCE; xác nhận đây là bản ZIP không có metadata Git.
3. Kiểm tra `E:\BE_Service`; thư mục chưa tồn tại.
4. Chạy `git clone https://github.com/Microservice-Project-w3w/BE_Service.git E:\BE_Service`; clone thành công.
5. Kiểm tra TARGET: origin đúng URL; ban đầu ở `main`, sạch; log gồm `e47dc50 changing`, `67e3133 first commit`.
6. Chạy `git fetch origin --prune`; remote có `origin/main`, `origin/rental-service`, không có `origin/organization-customer-service`.
7. Chạy `git switch -c organization-customer-service origin/main`; không overwrite branch nào.
8. Kiểm tra `git merge-base HEAD origin/main`; merge-base trùng HEAD, có lịch sử chung, chưa có thay đổi người khác trên branch mới.
9. So sánh POM, controller, service, repository, entity, DTO, exception, security, migration, shared dependency và test. Không copy code.

Không chạy: `reset`, `stash`, `restore`, `clean`, `rebase`, `merge`, `commit`, `push`.

## 3. Kết quả so sánh kiến trúc

- TARGET giữ root POM riêng (`equipment-rental-microservices:0.1.0-SNAPSHOT`) và chỉ khai báo các module hiện hữu. Không được thay bằng root POM SOURCE (`equipment-rental-backend:1.0.0-SNAPSHOT`).
- TARGET organization service hiện chỉ có `pom.xml`, `README.md`, `OrganizationCustomerServiceApplication`, `HealthController`, `application.yml`; chưa có API nghiệp vụ.
- SOURCE service dùng Lombok trong entity/service/controller nhưng TARGET service POM chưa có Lombok.
- SOURCE có `shared/common-security`, `shared/common-web`, `shared/event-contracts`; TARGET không có ba module này. Tuy nhiên code 36 API hiện không import các shared module, nên không nên copy tự động. Authorization hiện cũng chưa được tích hợp.
- `infra/mysql/init.sql` của TARGET chỉ tạo database, chưa tạo 8 bảng: `organizations`, `branches`, `employees`, `employee_branch_assignments`, `customers`, `customer_groups`, `customer_group_members`, `restricted_customers`.
- SOURCE không có Flyway/Liquibase migration và cấu hình `ddl-auto: none`; vì vậy schema là dependency bắt buộc cần thiết kế/đối chiếu trước khi endpoint chạy.
- SOURCE và TARGET đều không có test nghiệp vụ thực tế (chỉ placeholder ở SOURCE). Test cho từng API phải bổ sung.
- Không sửa Identity, Inventory, Rental, Logistics, Billing, Maintenance. TARGET có thêm AI service và các thay đổi kiến trúc riêng cần giữ nguyên.
- Không có xung đột Git/lịch sử. Có **khoảng trống tích hợp**: parent coordinates khác, Lombok thiếu, schema thiếu, security/organization-scope chưa được enforce.

### Đề xuất foundation commit (ngoài 36 API, cần chủ dự án quyết định)

`chore(organization-customer): establish service foundation`

Phạm vi tối thiểu: cập nhật riêng `services/organization-customer-service/pom.xml` theo parent TARGET; thêm Lombok và test dependencies cần thiết; thêm exception handler/enums/base entities-repositories dùng chung; thêm migration/schema 8 bảng theo chuẩn TARGET; tạo test profile/base fixture. Không đưa shared modules vào nếu chưa có import thực tế. Vì foundation phục vụ nhiều API và không thể gắn trung thực cho riêng API Create Organization, commit này nên đứng trước commit #1; nếu bắt buộc tổng lịch sử chỉ đúng 36 commit thì cần người dùng quyết định cách gom foundation mà vẫn giữ build được.

## 4. Danh mục chính xác 36 API

Quy ước quyền trong bảng: `CREATE/VIEW/UPDATE/DELETE` là quyền **cần thiết dự kiến** theo `PermissionConstants`; implementation SOURCE hiện không có Spring Security hay `@PreAuthorize`, nên tất cả đang là **chưa enforce**. Mỗi test dự kiến gồm contract MockMvc + service/repository integration + authorization/scope organization và regression.

| # | Method và endpoint đầy đủ | Controller → service | DTO | Repository/dependency | Quyền cần thiết | Hiện trạng và test cần bổ sung |
|---:|---|---|---|---|---|---|
| 1 | `POST /api/v1/organizations` | OrganizationController → `create` | OrganizationRequest/Response | OrganizationRepository | CREATE | Chỉ SOURCE; test 201, validation, duplicate code/tax, auth |
| 2 | `GET /api/v1/organizations` | OrganizationController → `getAll` | OrganizationResponse | OrganizationRepository | VIEW | Chỉ SOURCE; test 200/list/soft-delete/auth |
| 3 | `GET /api/v1/organizations/{id}` | OrganizationController → `getById` | OrganizationResponse | OrganizationRepository | VIEW | Chỉ SOURCE; test 200/404/auth |
| 4 | `PUT /api/v1/organizations/{id}` | OrganizationController → `update` | OrganizationRequest/Response | OrganizationRepository | UPDATE | Chỉ SOURCE; test 200/validation/conflict/404/auth |
| 5 | `DELETE /api/v1/organizations/{id}?actorUserId=` | OrganizationController → `delete` | — | OrganizationRepository | DELETE | Chỉ SOURCE; test 204/soft-delete/404/auth |
| 6 | `POST /api/v1/organizations/{organizationId}/branches` | BranchController → `create` | BranchRequest/Response | BranchRepository; OrganizationService | CREATE | Chỉ SOURCE; test 201/duplicate/org scope/auth |
| 7 | `GET /api/v1/organizations/{organizationId}/branches` | BranchController → `getAll` | BranchResponse | BranchRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/soft-delete/org scope/auth |
| 8 | `GET /api/v1/organizations/{organizationId}/branches/{branchId}` | BranchController → `getById` | BranchResponse | BranchRepository; OrganizationService | VIEW | Chỉ SOURCE; test 200/404/cross-org/auth |
| 9 | `PUT /api/v1/organizations/{organizationId}/branches/{branchId}` | BranchController → `update` | BranchRequest/Response | BranchRepository; OrganizationService | UPDATE | Chỉ SOURCE; test validation/conflict/scope/auth |
| 10 | `DELETE /api/v1/organizations/{organizationId}/branches/{branchId}?actorUserId=` | BranchController → `delete` | — | BranchRepository; OrganizationService | DELETE | Chỉ SOURCE; test 204/soft-delete/scope/auth |
| 11 | `POST /api/v1/organizations/{organizationId}/employees` | EmployeeController → `create` | EmployeeRequest/Response | EmployeeRepository; OrganizationService | CREATE | Chỉ SOURCE; test 201/duplicate code-user/scope/auth |
| 12 | `GET /api/v1/organizations/{organizationId}/employees` | EmployeeController → `getAll` | EmployeeResponse | EmployeeRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/soft-delete/scope/auth |
| 13 | `GET /api/v1/organizations/{organizationId}/employees/{employeeId}` | EmployeeController → `getById` | EmployeeResponse | EmployeeRepository; OrganizationService | VIEW | Chỉ SOURCE; test 200/404/cross-org/auth |
| 14 | `PUT /api/v1/organizations/{organizationId}/employees/{employeeId}` | EmployeeController → `update` | EmployeeRequest/Response | EmployeeRepository; OrganizationService | UPDATE | Chỉ SOURCE; test validation/conflict/scope/auth |
| 15 | `DELETE /api/v1/organizations/{organizationId}/employees/{employeeId}?actorUserId=` | EmployeeController → `delete` | — | EmployeeRepository; OrganizationService | DELETE | Chỉ SOURCE; test 204/soft-delete/scope/auth |
| 16 | `POST /api/v1/organizations/{organizationId}/employee-branch-assignments` | EmployeeBranchAssignmentController → `create` | EmployeeBranchAssignmentRequest/Response | AssignmentRepository; Organization/Employee/BranchService | CREATE | Chỉ SOURCE; test 201/duplicate/active rules/scope/auth |
| 17 | `GET /api/v1/organizations/{organizationId}/employee-branch-assignments?employeeId=` | EmployeeBranchAssignmentController → `getAll` | EmployeeBranchAssignmentResponse | AssignmentRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/filter/scope/auth |
| 18 | `PATCH /api/v1/organizations/{organizationId}/employee-branch-assignments/{assignmentId}/deactivate?actorUserId=` | EmployeeBranchAssignmentController → `deactivate` | EmployeeBranchAssignmentResponse | AssignmentRepository; OrganizationService | UPDATE | Chỉ SOURCE; test status/idempotency/404/scope/auth |
| 19 | `POST /api/v1/organizations/{organizationId}/customers` | CustomerController → `create` | CustomerRequest/Response | CustomerRepository; Organization/BranchService | CREATE | Chỉ SOURCE; test individual/business validation/duplicate/scope/auth |
| 20 | `GET /api/v1/organizations/{organizationId}/customers?branchId=&customerType=&ownerUserId=&q=` | CustomerController → `getAll` | CustomerResponse | CustomerRepository/JPA Specification; Organization/BranchService | VIEW | Chỉ SOURCE; test each filter/combinations/scope/auth |
| 21 | `GET /api/v1/organizations/{organizationId}/customers/{customerId}` | CustomerController → `getById` | CustomerResponse | CustomerRepository; OrganizationService | VIEW | Chỉ SOURCE; test 200/404/cross-org/auth |
| 22 | `PUT /api/v1/organizations/{organizationId}/customers/{customerId}` | CustomerController → `update` | CustomerRequest/Response | CustomerRepository; Organization/BranchService | UPDATE | Chỉ SOURCE; test types/validation/conflict/scope/auth |
| 23 | `DELETE /api/v1/organizations/{organizationId}/customers/{customerId}?actorUserId=` | CustomerController → `delete` | — | CustomerRepository; OrganizationService | DELETE | Chỉ SOURCE; test 204/soft-delete/scope/auth |
| 24 | `GET /api/v1/organizations/{organizationId}/customers/{customerId}/ownership?userId=` | CustomerController → `checkOwnership` | OwnershipResponse | CustomerRepository; OrganizationService | VIEW | Chỉ SOURCE; test true/false/404/scope/auth |
| 25 | `POST /api/v1/organizations/{organizationId}/customer-groups` | CustomerGroupController → `create` | CustomerGroupRequest/Response | CustomerGroupRepository; OrganizationService | CREATE | Chỉ SOURCE; test 201/duplicate/scope/auth |
| 26 | `GET /api/v1/organizations/{organizationId}/customer-groups` | CustomerGroupController → `getAll` | CustomerGroupResponse | CustomerGroupRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/soft-delete/scope/auth |
| 27 | `GET /api/v1/organizations/{organizationId}/customer-groups/{groupId}` | CustomerGroupController → `getById` | CustomerGroupResponse | CustomerGroupRepository; OrganizationService | VIEW | Chỉ SOURCE; test 200/404/cross-org/auth |
| 28 | `PUT /api/v1/organizations/{organizationId}/customer-groups/{groupId}` | CustomerGroupController → `update` | CustomerGroupRequest/Response | CustomerGroupRepository; OrganizationService | UPDATE | Chỉ SOURCE; test validation/conflict/scope/auth |
| 29 | `DELETE /api/v1/organizations/{organizationId}/customer-groups/{groupId}?actorUserId=` | CustomerGroupController → `delete` | — | Group/MemberRepository; OrganizationService | DELETE | Chỉ SOURCE; test 204/soft-delete/member rule/scope/auth |
| 30 | `POST /api/v1/organizations/{organizationId}/customer-groups/{groupId}/members/{customerId}?actorUserId=` | CustomerGroupController → `addMember` | CustomerGroupMemberResponse | Group/MemberRepository; Organization/CustomerService | UPDATE | Chỉ SOURCE; test 201/duplicate/customer status/scope/auth |
| 31 | `GET /api/v1/organizations/{organizationId}/customer-groups/{groupId}/members` | CustomerGroupController → `getMembers` | CustomerGroupMemberResponse | Group/MemberRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/404/scope/auth |
| 32 | `DELETE /api/v1/organizations/{organizationId}/customer-groups/{groupId}/members/{customerId}` | CustomerGroupController → `removeMember` | — | Group/MemberRepository; OrganizationService | UPDATE | Chỉ SOURCE; test 204/404/scope/auth |
| 33 | `POST /api/v1/organizations/{organizationId}/restricted-customers` | RestrictedCustomerController → `create` | RestrictedCustomerRequest/Response | RestrictedCustomerRepository; Organization/CustomerService | CREATE | Chỉ SOURCE; test 201/duplicate type/dates/scope/auth |
| 34 | `GET /api/v1/organizations/{organizationId}/restricted-customers?customerId=` | RestrictedCustomerController → `getAll` | RestrictedCustomerResponse | RestrictedCustomerRepository; OrganizationService | VIEW | Chỉ SOURCE; test list/filter/scope/auth |
| 35 | `GET /api/v1/organizations/{organizationId}/restricted-customers/check/{customerId}` | RestrictedCustomerController → `check` | RestrictionCheckResponse | RestrictedCustomerRepository; Organization/CustomerService | VIEW | Chỉ SOURCE; test active/expired/none/scope/auth |
| 36 | `PATCH /api/v1/organizations/{organizationId}/restricted-customers/{restrictionId}/remove` | RestrictedCustomerController → `remove` | RemoveRestrictionRequest/RestrictedCustomerResponse | RestrictedCustomerRepository; OrganizationService | UPDATE | Chỉ SOURCE; test removal validation/status/404/scope/auth |

## 5. Kế hoạch 36 commit / 4 ngày

`Files liên quan` là phạm vi dự kiến, không phải file đã thay đổi. Mọi commit phải chứa implementation và test thật, build được cùng lịch sử trước đó.

| STT | Ngày | API | Commit message | Files liên quan | Dependencies | Trạng thái |
|---:|---:|---|---|---|---|---|
| 1 | 1 | Organization Create | `feat(organization): add create organization API` | OrganizationController/Service/Repository; OrganizationRequest/Response; Organization API tests | foundation, Organization entity | Chờ duyệt |
| 2 | 1 | Organization List | `feat(organization): add list organizations API` | OrganizationController/Service/Repository; response; tests | #1 | Chờ duyệt |
| 3 | 1 | Organization Detail | `feat(organization): add organization detail API` | OrganizationController/Service/Repository; response; tests | #1 | Chờ duyệt |
| 4 | 1 | Organization Update | `feat(organization): add update organization API` | OrganizationController/Service/Repository; request/response; tests | #1,#3 | Chờ duyệt |
| 5 | 1 | Organization Deactivate | `feat(organization): add deactivate organization API` | OrganizationController/Service/Repository; tests | #1,#3 | Chờ duyệt |
| 6 | 1 | Branch Create | `feat(branch): add create branch API` | BranchController/Service/Repository; Branch entity/request/response; tests | #1,#3 | Chờ duyệt |
| 7 | 1 | Branch List | `feat(branch): add list branches API` | BranchController/Service/Repository; response; tests | #6 | Chờ duyệt |
| 8 | 1 | Branch Detail | `feat(branch): add branch detail API` | BranchController/Service/Repository; response; tests | #6 | Chờ duyệt |
| 9 | 1 | Branch Update | `feat(branch): add update branch API` | BranchController/Service/Repository; request/response; tests | #6,#8 | Chờ duyệt |
| 10 | 2 | Branch Deactivate | `feat(branch): add deactivate branch API` | BranchController/Service/Repository; tests | #6,#8 | Chờ duyệt |
| 11 | 2 | Employee Create | `feat(employee): add create employee API` | EmployeeController/Service/Repository; Employee entity/request/response; tests | #1,#3 | Chờ duyệt |
| 12 | 2 | Employee List | `feat(employee): add list employees API` | EmployeeController/Service/Repository; response; tests | #11 | Chờ duyệt |
| 13 | 2 | Employee Detail | `feat(employee): add employee detail API` | EmployeeController/Service/Repository; response; tests | #11 | Chờ duyệt |
| 14 | 2 | Employee Update | `feat(employee): add update employee API` | EmployeeController/Service/Repository; request/response; tests | #11,#13 | Chờ duyệt |
| 15 | 2 | Employee Deactivate | `feat(employee): add deactivate employee API` | EmployeeController/Service/Repository; tests | #11,#13 | Chờ duyệt |
| 16 | 2 | Assign employee to branch | `feat(assignment): add employee branch assignment API` | AssignmentController/Service/Repository; entity/request/response; tests | #6,#11 | Chờ duyệt |
| 17 | 2 | List assignments | `feat(assignment): add list assignments API` | AssignmentController/Service/Repository; response; tests | #16 | Chờ duyệt |
| 18 | 2 | Deactivate assignment | `feat(assignment): add deactivate assignment API` | AssignmentController/Service/Repository; response; tests | #16 | Chờ duyệt |
| 19 | 3 | Customer Create | `feat(customer): add create customer API` | CustomerController/Service/Repository; Customer entity/enums/request/response; tests | #1,#6 | Chờ duyệt |
| 20 | 3 | Customer List/Search | `feat(customer): add customer search API` | CustomerController/Service/Repository specifications; response; tests | #19 | Chờ duyệt |
| 21 | 3 | Customer Detail | `feat(customer): add customer detail API` | CustomerController/Service/Repository; response; tests | #19 | Chờ duyệt |
| 22 | 3 | Customer Update | `feat(customer): add update customer API` | CustomerController/Service/Repository; request/response; tests | #19,#21 | Chờ duyệt |
| 23 | 3 | Customer Deactivate | `feat(customer): add deactivate customer API` | CustomerController/Service/Repository; tests | #19,#21 | Chờ duyệt |
| 24 | 3 | Customer Ownership | `feat(customer): add ownership check API` | CustomerController/Service/Repository; OwnershipResponse; tests | #19,#21 | Chờ duyệt |
| 25 | 3 | Customer Group Create | `feat(customer-group): add create group API` | GroupController/Service/Repository; group entity/request/response; tests | #1 | Chờ duyệt |
| 26 | 3 | Customer Group List | `feat(customer-group): add list groups API` | GroupController/Service/Repository; response; tests | #25 | Chờ duyệt |
| 27 | 3 | Customer Group Detail | `feat(customer-group): add group detail API` | GroupController/Service/Repository; response; tests | #25 | Chờ duyệt |
| 28 | 4 | Customer Group Update | `feat(customer-group): add update group API` | GroupController/Service/Repository; request/response; tests | #25,#27 | Chờ duyệt |
| 29 | 4 | Customer Group Delete | `feat(customer-group): add delete group API` | GroupController/Service/Repository; tests | #25,#27 | Chờ duyệt |
| 30 | 4 | Add group member | `feat(customer-group): add group member API` | GroupController/Service; MemberRepository/entity/response; tests | #19,#25 | Chờ duyệt |
| 31 | 4 | List group members | `feat(customer-group): add list group members API` | GroupController/Service; MemberRepository/response; tests | #30 | Chờ duyệt |
| 32 | 4 | Remove group member | `feat(customer-group): add remove group member API` | GroupController/Service; MemberRepository; tests | #30 | Chờ duyệt |
| 33 | 4 | Create restriction | `feat(restricted-customer): add create restriction API` | RestrictedController/Service/Repository; entity/enums/request/response; tests | #19,#21 | Chờ duyệt |
| 34 | 4 | List restrictions | `feat(restricted-customer): add list restrictions API` | RestrictedController/Service/Repository; response; tests | #33 | Chờ duyệt |
| 35 | 4 | Check restriction | `feat(restricted-customer): add restriction check API` | RestrictedController/Service/Repository; RestrictionCheckResponse; tests | #33 | Chờ duyệt |
| 36 | 4 | Remove restriction | `feat(restricted-customer): add remove restriction API` | RestrictedController/Service/Repository; RemoveRestrictionRequest/response; tests | #33 | Chờ duyệt |

## 6. Chiến lược kiểm thử cho mỗi commit

1. Compile module cùng các dependency được reactor quản lý: `mvn -pl services/organization-customer-service -am compile`.
2. Unit test service cho happy path, validation/business rule, not-found/conflict và soft-delete/status transition.
3. MockMvc contract test cho method/path, params/body validation, response/status và exception mapping.
4. Repository integration test (MySQL-compatible/Testcontainers nếu quy ước repo cho phép) cho query, uniqueness và organization scope.
5. Security test: unauthenticated/forbidden/allowed permission và cross-organization access. Vì SOURCE chưa có security enforcement, đây là hạng mục thiết kế bắt buộc trước khi tuyên bố hoàn thành.
6. Regression: chạy toàn bộ test của organization-customer-service sau từng commit; trước push theo ngày chạy reactor tests liên quan, không đụng service khác.

Trong giai đoạn khảo sát này chưa chạy compile/test vì chưa chuyển code và TARGET chỉ là skeleton. Kết quả thực tế: **SKIPPED**, không tuyên bố PASS.

## 7. Kế hoạch merge và trạng thái cuối

- Mọi triển khai chỉ trên local branch `organization-customer-service`.
- Sau mỗi ngày chỉ commit/push khi người dùng cho phép rõ ràng.
- Khi đủ API và kiểm thử, chuẩn bị PR `organization-customer-service -> main`.
- Dự kiến merge commit giữ lịch sử, không squash, nếu policy repository cho phép; không merge khi chưa được yêu cầu.

```text
CURRENT BRANCH = organization-customer-service
WORKING TREE = modified only by this requested report file
FILES MODIFIED = docs/ORGANIZATION_CUSTOMER_MIGRATION_PLAN.md (new, untracked)
COMMITS CREATED = 0
PUSH PERFORMED = NO
MAIN TOUCHED = NO
READY FOR DAY 1 = NO (cần xác nhận phương án foundation/schema/security và lệnh bắt đầu Ngày 1)
```

## 8. Kết luận và bước tiếp theo

Đã hoàn thành xác định SOURCE/TARGET, clone, xác minh origin, tạo branch đúng từ main, kiểm tra merge-base, thống kê 36 API và lập kế hoạch 4 ngày. Chưa chuyển code, chưa chạy test, chưa commit/push/merge. Khi được xác nhận bắt đầu Ngày 1, việc đầu tiên là chốt foundation commit/schema/security; sau đó triển khai đúng 9 API Ngày 1 theo thứ tự, không tạo trước commit của ngày sau.

## 9. Cập nhật chuẩn bị foundation và API 1 — 2026-09-28

### Quyết định kế hoạch

- Đã xác nhận tổng cộng **37 commit**: 1 foundation commit và 36 API commit.
- 36 API vẫn chia 4 ngày, mỗi ngày 9 API.
- Chưa tạo commit, chưa push, chưa merge.

### Foundation đã chuẩn bị

- Giữ nguyên parent và module structure của TARGET; chỉ cập nhật POM của organization-customer-service.
- Bổ sung Lombok và bật annotation processing `proc=full` tại module vì máy dùng Maven với JDK 23; không sửa root POM.
- Bổ sung Flyway Core + Flyway MySQL và bật migration tại `classpath:db/migration`.
- Bổ sung H2 test-scope và profile `test`, chạy database in-memory ở MySQL compatibility mode.
- Chuyển đúng 9 enum, 8 JPA entity, 8 Spring Data repository và 4 exception/handler từ SOURCE duy nhất.
- Không chuyển `shared/common-security`, `shared/common-web`, `shared/event-contracts` vì code hiện tại chưa import và TARGET chưa khai báo kiến trúc shared module.

### Migration/schema

Đã tạo `V1__create_organization_customer_schema.sql` với 8 bảng: `organizations`, `branches`, `employees`, `employee_branch_assignments`, `customers`, `customer_groups`, `customer_group_members`, `restricted_customers`; gồm primary key, quan hệ nội-service, unique constraint và index phục vụ scope/search. Không chạy migration trên MySQL thật. Migration chỉ được thực thi trong H2 in-memory khi test và đã qua Hibernate `ddl-auto=validate`.

### Security

TARGET hiện chưa có implementation JWT/resource server ở Gateway, Identity hay service; Identity mới là skeleton. README quy định token tương lai mang role, `organizationId`, `branchIds` và downstream service tự authorize. Do chưa có issuer/JWK, claim names hoặc permission contract thực tế, chưa thêm Spring Security/JWT filter và không tin cậy header tự tạo. API 1 hiện chưa được bảo vệ; cần đồng bộ contract Identity trước khi đánh dấu authorization hoàn tất.

### API 1 — Create Organization

Đã chuẩn bị riêng `POST /api/v1/organizations`: OrganizationRequest/Response, controller chỉ có POST, service chỉ có `create`, repository foundation và test. Chưa đưa List/Detail/Update/Delete vào controller/service. Behavior đã kiểm tra: HTTP 201, Bean Validation 400, mặc định status ACTIVE, chuẩn hóa optional string, chống trùng organization code/tax code ở service.

### Kết quả kiểm tra thực tế

- Lần compile đầu: **FAIL** vì lệnh `mvn` không có trong PATH.
- Maven cache 3.9.16 được tìm thấy; lần chạy với JAVA_HOME mặc định JDK 11: **FAIL** (`release version 21 not supported`).
- Chạy Maven 3.9.16 với `JAVA_HOME=C:\Program Files\Java\jdk-23`; compile ban đầu **FAIL** do Lombok annotation processing chưa bật.
- Sau khi thêm `maven-compiler-plugin` với `proc=full`: compile **PASS**, 35 source files.
- `mvn ... test`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped. Gồm 2 controller contract tests, 2 service unit tests và 1 Spring context/Flyway/Hibernate schema validation test.
- MySQL thật: **SKIPPED**, không kết nối và không thay đổi dữ liệu.

### Phân tách file dự kiến cho hai commit đầu

Foundation commit dự kiến gồm module POM/config, toàn bộ `entity/`, `enums/`, `exception/`, `repository/`, Flyway V1, `application-test.yml`, `SchemaMigrationTest.java`. API 1 commit dự kiến chỉ gồm Organization request/response, OrganizationController, OrganizationService, OrganizationControllerTest và OrganizationServiceTest. Việc stage/commit sẽ chỉ thực hiện sau khi người dùng duyệt.

```text
COMMITS CREATED = 0
PUSH PERFORMED = NO
MAIN TOUCHED = NO
NEXT PROPOSED COMMIT = chore(organization-customer): establish service foundation
```
