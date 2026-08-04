# Equipment Rental Backend

Backend quản lý cho thuê thiết bị theo kiến trúc SOA/microservices. Đây là Maven monorepo chứa nhiều Spring Boot application độc lập; không có một Spring Boot Application chung ở root.

## Yêu cầu cài đặt

- JDK 21
- Maven 3.9+
- Docker Desktop hoặc Docker Engine có Docker Compose
- IntelliJ IDEA

## Cấu trúc chính

```text
equipment-rental-backend/
├── api-gateway/
├── services/
│   ├── identity-service/
│   ├── organization-customer-service/
│   ├── inventory-service/
│   ├── rental-service/
│   ├── logistics-service/
│   ├── billing-service/
│   └── maintenance-service/
├── shared/
│   ├── common-web/
│   ├── common-security/
│   └── event-contracts/
├── infra/
└── docs/
```

## Cấu hình môi trường

```bash
cp .env.example .env
```

Docker Compose tự đọc `.env` ở root. Khi chạy Spring Boot trực tiếp, cấu hình các biến cần thiết trong Run Configuration của IntelliJ hoặc môi trường terminal. Nếu không khai báo, cấu hình local mặc định trong `application.yml` sẽ được dùng.

## Chạy hạ tầng

```bash
docker compose -f infra/docker-compose.yml up -d
```

Hạ tầng local gồm MySQL, RabbitMQ Management và Redis. MySQL chỉ tạo tên bảy database, chưa tạo bảng nghiệp vụ.

## Build toàn bộ dự án

```bash
mvn clean install
```

## Chạy từng service

```bash
mvn spring-boot:run -pl services/identity-service
mvn spring-boot:run -pl services/organization-customer-service
mvn spring-boot:run -pl services/inventory-service
mvn spring-boot:run -pl services/rental-service
mvn spring-boot:run -pl services/logistics-service
mvn spring-boot:run -pl services/billing-service
mvn spring-boot:run -pl services/maintenance-service
mvn spring-boot:run -pl api-gateway
```

## Health endpoints

```text
http://localhost:8080/health  api-gateway
http://localhost:8081/health  identity-service
http://localhost:8082/health  organization-customer-service
http://localhost:8083/health  inventory-service
http://localhost:8084/health  rental-service
http://localhost:8085/health  logistics-service
http://localhost:8086/health  billing-service
http://localhost:8087/health  maintenance-service
```

Mỗi service chỉ sở hữu source code và database của chính nó. Tích hợp đồng bộ qua HTTP; tích hợp bất đồng bộ qua event/RabbitMQ.
