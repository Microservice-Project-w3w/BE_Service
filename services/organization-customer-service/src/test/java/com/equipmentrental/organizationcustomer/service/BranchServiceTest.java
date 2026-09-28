package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.entity.Branch;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.repository.BranchRepository;
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
class BranchServiceTest {
    @Mock BranchRepository branchRepository;
    @Mock OrganizationService organizationService;
    @InjectMocks BranchService branchService;

    @Test
    void createScopesBranchToOrganizationAndDefaultsStatus() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(branchRepository.save(any())).thenAnswer(invocation -> {
            Branch branch = invocation.getArgument(0); branch.setId(2L); return branch;
        });
        var result = branchService.create(1L, new BranchRequest("BR-01", "Main", null,
                null, null, null, 7L));
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(BranchStatus.ACTIVE);
    }

    @Test
    void createRejectsDuplicateCodeWithinOrganization() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(branchRepository.existsByOrganizationIdAndBranchCodeAndDeletedAtIsNull(1L, "BR-01"))
                .thenReturn(true);
        var request = new BranchRequest("BR-01", "Main", null, null, null, null, null);
        assertThatThrownBy(() -> branchService.create(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void getAllScopesQueryToOrganization() {
        when(organizationService.getEntity(1L)).thenReturn(new Organization());
        when(branchRepository.findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(1L))
                .thenReturn(List.of(Branch.builder().id(2L).organizationId(1L).branchCode("BR-01")
                        .branchName("Main").status(BranchStatus.ACTIVE).build()));
        var result = branchService.getAll(1L);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().organizationId()).isEqualTo(1L);
    }
}
