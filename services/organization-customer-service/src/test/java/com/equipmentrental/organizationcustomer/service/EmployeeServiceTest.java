package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeRequest;
import com.equipmentrental.organizationcustomer.entity.Employee;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock EmployeeRepository employeeRepository;
    @Mock OrganizationService organizationService;
    @InjectMocks EmployeeService employeeService;

    @Test
    void createScopesEmployeeAndDefaultsStatus() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0); employee.setId(2L); return employee;
        });
        var result = employeeService.create(1L, new EmployeeRequest(9L, "EMP-01", " A ",
                " ", null, null, null, null, 7L));
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.fullName()).isEqualTo("A");
        assertThat(result.email()).isNull();
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(employeeRepository.existsByOrganizationIdAndEmployeeCodeAndDeletedAtIsNull(1L, "EMP-01"))
                .thenReturn(true);
        var request = new EmployeeRequest(null, "EMP-01", "A", null, null, null, null, null, null);
        assertThatThrownBy(() -> employeeService.create(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createRejectsDuplicateUserWithinOrganization() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(employeeRepository.existsByOrganizationIdAndUserIdAndDeletedAtIsNull(1L, 9L)).thenReturn(true);
        var request = new EmployeeRequest(9L, "EMP-01", "A", null, null, null, null, null, null);
        assertThatThrownBy(() -> employeeService.create(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void getAllScopesToOrganizationAndExcludesDeletedEmployees() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(employeeRepository.findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(1L))
                .thenReturn(List.of(Employee.builder().id(2L).organizationId(1L).employeeCode("EMP-01")
                        .fullName("A").status(EmployeeStatus.ACTIVE).build()));
        assertThat(employeeService.getAll(1L)).singleElement()
                .satisfies(employee -> assertThat(employee.organizationId()).isEqualTo(1L));
    }
}
