# Tổng quan hệ thống

Hệ thống quản lý cho thuê thiết bị dùng kiến trúc microservice. Client trong tương lai chỉ truy cập qua API Gateway; các service không phải là điểm tích hợp công khai trực tiếp.

Mỗi service sở hữu nghiệp vụ, mã nguồn và database riêng. Identity Service chịu trách nhiệm authentication, user, role, permission và session đăng nhập. Authorization dùng RBAC kết hợp data scope: permission xác định thao tác được phép, còn organizationId, branchId và ownerId quyết định dữ liệu được phép thao tác.

REST được dùng cho các lời gọi giữa service cần kết quả ngay. Event sẽ được bổ sung sau cho các luồng bất đồng bộ. AI, khi được bổ sung, không được truy cập database trực tiếp mà chỉ đi qua API/service được kiểm soát.

Các entity quan trọng cần có `organizationId` và `branchId` khi nghiệp vụ giới hạn theo tổ chức hoặc chi nhánh. Không JOIN database giữa các service và không tạo foreign key xuyên service.
