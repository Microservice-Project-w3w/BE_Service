package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.CustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerResponse;
import com.equipmentrental.organizationcustomer.dto.response.OwnershipResponse;
import com.equipmentrental.organizationcustomer.entity.Customer;
import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.enums.CustomerType;
import com.equipmentrental.organizationcustomer.exception.BadRequestException;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final OrganizationService organizationService;

    private final BranchService branchService;


    // =====================================================
    // 1. TẠO KHÁCH HÀNG
    // =====================================================

    public CustomerResponse create(
            Long organizationId,
            CustomerRequest request
    ) {

        // Doanh nghiệp phải tồn tại
        organizationService.getEntity(
                organizationId
        );

        // Nếu có branchId thì branch phải thuộc organization này
        validateBranch(
                organizationId,
                request.branchId()
        );

        // Validate theo loại khách hàng
        validateCustomerType(request);

        // Không cho trùng customerCode trong cùng doanh nghiệp
        if (customerRepository
                .existsByOrganizationIdAndCustomerCodeAndDeletedAtIsNull(
                        organizationId,
                        request.customerCode()
                )) {

            throw new ConflictException(
                    "Mã khách hàng đã tồn tại trong doanh nghiệp"
            );
        }


        Customer customer = Customer.builder()

                .organizationId(
                        organizationId
                )

                .branchId(
                        request.branchId()
                )

                .ownerUserId(
                        request.ownerUserId()
                )

                .customerCode(
                        request.customerCode().trim()
                )

                .customerType(
                        request.customerType()
                )

                .displayName(
                        request.displayName().trim()
                )

                .email(
                        clean(request.email())
                )

                .phone(
                        clean(request.phone())
                )

                .address(
                        clean(request.address())
                )

                // Cá nhân
                .fullName(
                        request.customerType()
                                == CustomerType.INDIVIDUAL
                                ? clean(request.fullName())
                                : null
                )

                .dateOfBirth(
                        request.customerType()
                                == CustomerType.INDIVIDUAL
                                ? request.dateOfBirth()
                                : null
                )

                .identityNumber(
                        request.customerType()
                                == CustomerType.INDIVIDUAL
                                ? clean(request.identityNumber())
                                : null
                )

                // Doanh nghiệp
                .companyName(
                        request.customerType()
                                == CustomerType.BUSINESS
                                ? clean(request.companyName())
                                : null
                )

                .taxCode(
                        request.customerType()
                                == CustomerType.BUSINESS
                                ? clean(request.taxCode())
                                : null
                )

                .representativeName(
                        request.customerType()
                                == CustomerType.BUSINESS
                                ? clean(request.representativeName())
                                : null
                )

                .representativePhone(
                        request.customerType()
                                == CustomerType.BUSINESS
                                ? clean(request.representativePhone())
                                : null
                )

                .representativeEmail(
                        request.customerType()
                                == CustomerType.BUSINESS
                                ? clean(request.representativeEmail())
                                : null
                )

                .status(
                        request.status() == null
                                ? CustomerStatus.ACTIVE
                                : request.status()
                )

                .note(
                        clean(request.note())
                )

                .createdBy(
                        request.actorUserId()
                )

                .updatedBy(
                        request.actorUserId()
                )

                .build();


        Customer saved =
                customerRepository.save(customer);

        return toResponse(saved);
    }


    // =====================================================
    // 2. DANH SÁCH + TÌM KIẾM + LỌC
    // =====================================================

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll(

            Long organizationId,

            Long branchId,

            CustomerType customerType,

            Long ownerUserId,

            String q
    ) {

        organizationService.getEntity(
                organizationId
        );


        /*
         * Bắt buộc mọi query phải nằm trong organization
         * hiện tại và chưa bị xóa mềm.
         */
        Specification<Customer> specification =
                (root, query, cb) ->
                        cb.and(
                                cb.equal(
                                        root.get("organizationId"),
                                        organizationId
                                ),
                                cb.isNull(
                                        root.get("deletedAt")
                                )
                        );


        // Lọc theo branch
        if (branchId != null) {

            // Branch phải thực sự thuộc organization
            branchService.getEntity(
                    organizationId,
                    branchId
            );

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("branchId"),
                                    branchId
                            )
            );
        }


        // Lọc khách cá nhân / doanh nghiệp
        if (customerType != null) {

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("customerType"),
                                    customerType
                            )
            );
        }


        // Lọc theo ownerUserId
        if (ownerUserId != null) {

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("ownerUserId"),
                                    ownerUserId
                            )
            );
        }


        // Search frontend / API
        if (q != null && !q.isBlank()) {

            String keyword =
                    "%" + q.trim().toLowerCase() + "%";


            specification = specification.and(
                    (root, query, cb) ->
                            cb.or(

                                    cb.like(
                                            cb.lower(
                                                    root.get("customerCode")
                                            ),
                                            keyword
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("displayName")
                                            ),
                                            keyword
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("email")
                                            ),
                                            keyword
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("phone")
                                            ),
                                            keyword
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("fullName")
                                            ),
                                            keyword
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("companyName")
                                            ),
                                            keyword
                                    )
                            )
            );
        }


        return customerRepository
                .findAll(
                        specification,
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. CHI TIẾT KHÁCH HÀNG
    // =====================================================

    @Transactional(readOnly = true)
    public CustomerResponse getById(

            Long organizationId,

            Long customerId
    ) {

        return toResponse(
                getEntity(
                        organizationId,
                        customerId
                )
        );
    }


    // =====================================================
    // 4. CẬP NHẬT KHÁCH HÀNG
    // =====================================================

    public CustomerResponse update(

            Long organizationId,

            Long customerId,

            CustomerRequest request
    ) {

        Customer customer =
                getEntity(
                        organizationId,
                        customerId
                );


        validateBranch(
                organizationId,
                request.branchId()
        );


        validateCustomerType(request);


        /*
         * Nếu customerCode thay đổi,
         * kiểm tra mã mới có bị trùng không.
         */
        if (!customer
                .getCustomerCode()
                .equals(request.customerCode())

                && customerRepository
                .existsByOrganizationIdAndCustomerCodeAndDeletedAtIsNull(
                        organizationId,
                        request.customerCode()
                )) {

            throw new ConflictException(
                    "Mã khách hàng đã tồn tại trong doanh nghiệp"
            );
        }


        customer.setBranchId(
                request.branchId()
        );

        customer.setOwnerUserId(
                request.ownerUserId()
        );

        customer.setCustomerCode(
                request.customerCode().trim()
        );

        customer.setCustomerType(
                request.customerType()
        );

        customer.setDisplayName(
                request.displayName().trim()
        );

        customer.setEmail(
                clean(request.email())
        );

        customer.setPhone(
                clean(request.phone())
        );

        customer.setAddress(
                clean(request.address())
        );


        // =================================================
        // Nếu chuyển thành khách cá nhân
        // =================================================

        if (request.customerType()
                == CustomerType.INDIVIDUAL) {

            customer.setFullName(
                    clean(request.fullName())
            );

            customer.setDateOfBirth(
                    request.dateOfBirth()
            );

            customer.setIdentityNumber(
                    clean(request.identityNumber())
            );


            // Xóa dữ liệu doanh nghiệp cũ
            customer.setCompanyName(null);
            customer.setTaxCode(null);
            customer.setRepresentativeName(null);
            customer.setRepresentativePhone(null);
            customer.setRepresentativeEmail(null);
        }


        // =================================================
        // Nếu chuyển thành khách doanh nghiệp
        // =================================================

        if (request.customerType()
                == CustomerType.BUSINESS) {

            customer.setCompanyName(
                    clean(request.companyName())
            );

            customer.setTaxCode(
                    clean(request.taxCode())
            );

            customer.setRepresentativeName(
                    clean(request.representativeName())
            );

            customer.setRepresentativePhone(
                    clean(request.representativePhone())
            );

            customer.setRepresentativeEmail(
                    clean(request.representativeEmail())
            );


            // Xóa dữ liệu cá nhân cũ
            customer.setFullName(null);
            customer.setDateOfBirth(null);
            customer.setIdentityNumber(null);
        }


        if (request.status() != null) {

            customer.setStatus(
                    request.status()
            );
        }


        customer.setNote(
                clean(request.note())
        );


        customer.setUpdatedBy(
                request.actorUserId()
        );


        Customer saved =
                customerRepository.save(customer);

        return toResponse(saved);
    }


    // =====================================================
    // 5. XÓA MỀM KHÁCH HÀNG
    // =====================================================

    public void delete(

            Long organizationId,

            Long customerId,

            Long actorUserId
    ) {

        Customer customer =
                getEntity(
                        organizationId,
                        customerId
                );


        customer.setStatus(
                CustomerStatus.DELETED
        );

        customer.setDeletedAt(
                LocalDateTime.now()
        );

        customer.setUpdatedBy(
                actorUserId
        );


        customerRepository.save(customer);
    }


    // =====================================================
    // 6. KIỂM TRA OWN
    // =====================================================

    @Transactional(readOnly = true)
    public OwnershipResponse checkOwnership(

            Long organizationId,

            Long customerId,

            Long userId
    ) {

        Customer customer =
                getEntity(
                        organizationId,
                        customerId
                );


        boolean owned =
                userId != null
                        && customer.getOwnerUserId() != null
                        && customer
                        .getOwnerUserId()
                        .equals(userId);


        return new OwnershipResponse(

                customerId,

                userId,

                owned
        );
    }


    // =====================================================
    // 7. LẤY CUSTOMER ENTITY NỘI BỘ
    // =====================================================

    public Customer getEntity(

            Long organizationId,

            Long customerId
    ) {

        return customerRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(
                        customerId,
                        organizationId
                )
                .orElseThrow(
                        () -> new NotFoundException(
                                "Không tìm thấy khách hàng id = "
                                        + customerId
                                        + " trong doanh nghiệp id = "
                                        + organizationId
                        )
                );
    }


    // =====================================================
    // 8. KIỂM TRA BRANCH
    // =====================================================

    private void validateBranch(

            Long organizationId,

            Long branchId
    ) {

        if (branchId == null) {
            return;
        }


        /*
         * Đây là điểm quan trọng của multi-tenant:
         *
         * Customer thuộc Organization 1
         * không được trỏ tới Branch của Organization 2.
         */
        branchService.getEntity(
                organizationId,
                branchId
        );
    }


    // =====================================================
    // 9. VALIDATE INDIVIDUAL / BUSINESS
    // =====================================================

    private void validateCustomerType(
            CustomerRequest request
    ) {

        if (request.customerType()
                == CustomerType.INDIVIDUAL) {

            if (request.fullName() == null
                    || request.fullName().isBlank()) {

                throw new BadRequestException(
                        "Khách hàng cá nhân phải có fullName"
                );
            }
        }


        if (request.customerType()
                == CustomerType.BUSINESS) {

            if (request.companyName() == null
                    || request.companyName().isBlank()) {

                throw new BadRequestException(
                        "Khách hàng doanh nghiệp phải có companyName"
                );
            }
        }
    }


    // =====================================================
    // 10. ENTITY -> RESPONSE
    // =====================================================

    private CustomerResponse toResponse(
            Customer customer
    ) {

        return new CustomerResponse(

                customer.getId(),

                customer.getOrganizationId(),

                customer.getBranchId(),

                customer.getOwnerUserId(),

                customer.getCustomerCode(),

                customer.getCustomerType(),

                customer.getDisplayName(),

                customer.getEmail(),

                customer.getPhone(),

                customer.getAddress(),

                // Cá nhân
                customer.getFullName(),

                customer.getDateOfBirth(),

                customer.getIdentityNumber(),

                // Doanh nghiệp
                customer.getCompanyName(),

                customer.getTaxCode(),

                customer.getRepresentativeName(),

                customer.getRepresentativePhone(),

                customer.getRepresentativeEmail(),

                customer.getStatus(),

                customer.getNote(),

                customer.getCreatedBy(),

                customer.getUpdatedBy(),

                customer.getCreatedAt(),

                customer.getUpdatedAt()
        );
    }


    // =====================================================
    // 11. STRING RỖNG -> NULL
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}