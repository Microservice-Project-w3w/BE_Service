# Sử dụng shared modules

Root project dùng `groupId` `com.equipmentrental` và version `1.0.0-SNAPSHOT`. Một service có thể thêm các dependency sau khi sẵn sàng tích hợp:

```xml
<dependency>
    <groupId>com.equipmentrental</groupId>
    <artifactId>common-web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.equipmentrental</groupId>
    <artifactId>common-security</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

`common-security` tự đăng ký cấu hình JWT resource server khi dependency có mặt. Cấu hình public endpoint theo service, ví dụ:

```yaml
app:
  security:
    public-paths:
      - /actuator/health
      - /actuator/info
      - /api/v1/auth/login
```

Kiểm tra permission ở controller bằng authority:

```java
@PreAuthorize("hasAuthority('rental.quotation.approve')")
```

Sau đó kiểm tra data scope trong service:

```java
CurrentUser user = currentUserProvider.getCurrentUser();

if (!dataScopeAuthorizer.canAccessBranch(
        user,
        entity.getOrganizationId(),
        entity.getBranchId())) {
    throw new BusinessException(
        CommonErrorCode.AUTH_DATA_SCOPE_DENIED
    );
}
```

Ví dụ trả lỗi nghiệp vụ:

```java
throw new BusinessException(
    CommonErrorCode.RESOURCE_NOT_FOUND,
    "Không tìm thấy báo giá"
);
```

Không thêm shared module vào mọi service một cách đồng loạt. Mỗi service cần cấu hình JWT decoder phù hợp và kiểm tra endpoint công khai của chính service trước khi tích hợp.
