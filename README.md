# Equipment Rental — Microservice Skeleton

Đây là backend mới, độc lập với `../backend` cũ. Không copy controller/entity/nghiệp vụ cũ sang repository này.

## Service ownership

| Owner | Module | Port | Database | Trách nhiệm ban đầu |
|---|---|---:|---|---|
| You | `api-gateway` | 8080 | — | Routing, CORS, gateway health, rate-limit/auth integration sau này |
| You | `ai-service` | 8090 | — | Chat API và adapter Ollama/AI provider |
| Dev 1 | `identity-service` | 8081 | `identity_db` | User, role, permission, authentication/session |
| Dev 2 | `organization-customer-service` | 8082 | `organization_customer_db` | Organization, branch, employee, customer |
| Dev 3 | `inventory-service` | 8083 | `inventory_db` | Category, equipment, warehouse, reservation |
| Dev 4 | `rental-service` | 8084 | `rental_db` | Rental request, quotation, order, contract |

Mỗi service sở hữu schema riêng, không đọc database của service khác. Giao tiếp liên-service phải qua HTTP/event contract, không dùng JPA entity chung.

## Start

```bash
cp .env.example .env
cd infra && docker compose up -d
mvn -pl api-gateway spring-boot:run
mvn -pl ai-service spring-boot:run
mvn -pl services/identity-service spring-boot:run
```

Chạy toàn bộ module sau khi các service có code: `mvn verify`.

## Public entrypoints

- Gateway: `http://localhost:8080`
- Gateway health: `GET /actuator/health`
- AI through Gateway: `POST /api/v1/ai/chat`
- AI direct (local only): `POST http://localhost:8090/api/v1/chat`

`ai-service` nhận `{ "message": "..." }` và trả `{ "answer": "...", "model": "...", "provider": "ollama" }`.
