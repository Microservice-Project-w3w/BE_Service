# Permission catalog

Permission có dạng `domain.resource.action`, ví dụ `rental.quotation.approve`, `inventory.stock.transfer` và `billing.deposit.refund`. Danh sách seed đầy đủ nằm ở [permission-seed.csv](permission-seed.csv).

Role không nên được hardcode trực tiếp vào nghiệp vụ khi có thể dùng permission. Controller nên kiểm tra authority, ví dụ:

```java
@PreAuthorize("hasAuthority('rental.quotation.approve')")
```

Role chỉ là một tập hợp permission. Permission không tự quyết định phạm vi dữ liệu: sau khi kiểm tra permission, service vẫn phải kiểm tra `organizationId`, `branchId` hoặc `ownerId`.
