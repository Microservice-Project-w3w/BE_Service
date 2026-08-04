# Kiến trúc hệ thống

Hệ thống sử dụng SOA/microservices trong một Git monorepo. Root Maven chỉ tổng hợp module để build; root không có Spring Boot Application.

## Quy tắc giao tiếp

- Frontend chỉ gọi API Gateway ở cổng `8080`.
- Service cần kết quả ngay gọi service sở hữu dữ liệu qua HTTP, đặt adapter trong package `client`.
- Service không cần kết quả ngay gửi event qua RabbitMQ, đặt publisher/consumer trong package `event`.
- Redis dành cho cache, distributed lock hoặc dữ liệu tạm; không thay thế database sở hữu của service.

## Quy tắc sở hữu dữ liệu

- Mỗi service chỉ truy cập database của chính nó.
- Không có khóa ngoại xuyên database.
- Không import Entity, Repository hoặc Service class giữa các service.
- Shared module chỉ chứa response model, security context và event contract ổn định; không chứa Entity nghiệp vụ.

## Cấu trúc ba lớp

```text
Controller → Service → Repository → Database riêng
```

- Controller định nghĩa route bằng annotation Spring MVC, validate request và trả response.
- Service chứa nghiệp vụ và transaction boundary.
- Repository chỉ truy cập database thuộc service hiện tại.
- Không tạo lớp route riêng.
