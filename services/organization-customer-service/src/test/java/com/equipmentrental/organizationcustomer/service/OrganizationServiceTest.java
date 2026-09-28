package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock OrganizationRepository repository;
    @InjectMocks OrganizationService service;

    @Test
    void createDefaultsStatusAndCleansOptionalValues() {
        var request = new OrganizationRequest("ORG-01", "Acme", " ", " info@example.com ", null, null, null, 7L);
        when(repository.save(any())).thenAnswer(invocation -> {
            Organization entity = invocation.getArgument(0); entity.setId(1L); return entity;
        });
        var response = service.create(request);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(OrganizationStatus.ACTIVE);
        assertThat(response.taxCode()).isNull();
        assertThat(response.email()).isEqualTo("info@example.com");
        assertThat(response.createdBy()).isEqualTo(7L);
    }

    @Test
    void createRejectsDuplicateOrganizationCode() {
        var request = new OrganizationRequest("ORG-01", "Acme", null, null, null, null, null, null);
        when(repository.existsByOrganizationCodeAndDeletedAtIsNull("ORG-01")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createRejectsDuplicateTaxCode() {
        var request = new OrganizationRequest("ORG-01", "Acme", "TAX-01", null, null, null, null, null);
        when(repository.existsByTaxCodeAndDeletedAtIsNull("TAX-01")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Mã số thuế đã tồn tại");
    }

    @Test
    void createMapsDatabaseConstraintViolationToConflict() {
        var request = new OrganizationRequest("ORG-01", "Acme", null, null, null, null, null, null);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Mã doanh nghiệp hoặc mã số thuế đã tồn tại");
    }

    @Test
    void getAllMapsActiveOrganizationsInRepositoryOrder() {
        Organization organization = Organization.builder().id(1L).organizationCode("ORG-01")
                .organizationName("Acme").status(OrganizationStatus.ACTIVE).build();
        when(repository.findAllByDeletedAtIsNullOrderByIdDesc()).thenReturn(List.of(organization));
        var result = service.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().organizationCode()).isEqualTo("ORG-01");
    }

    @Test
    void getByIdReturnsOrganization() {
        Organization organization = Organization.builder().id(1L).organizationCode("ORG-01")
                .organizationName("Acme").status(OrganizationStatus.ACTIVE).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(organization));
        assertThat(service.getById(1L).organizationName()).isEqualTo("Acme");
    }

    @Test
    void getByIdRejectsMissingOrganization() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(com.equipmentrental.organizationcustomer.exception.NotFoundException.class);
    }

    @Test
    void updateChangesOrganizationAndPreservesStatusWhenMissing() {
        Organization organization = Organization.builder().id(1L).organizationCode("ORG-01")
                .organizationName("Acme").status(OrganizationStatus.ACTIVE).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(organization));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new OrganizationRequest("ORG-01", "Acme Updated", null, null, null, null, null, 7L);
        var result = service.update(1L, request);
        assertThat(result.organizationName()).isEqualTo("Acme Updated");
        assertThat(result.status()).isEqualTo(OrganizationStatus.ACTIVE);
        assertThat(result.updatedBy()).isEqualTo(7L);
    }

    @Test
    void updateRejectsDuplicateOrganizationCode() {
        Organization organization = Organization.builder().id(1L).organizationCode("ORG-01")
                .organizationName("Acme").status(OrganizationStatus.ACTIVE).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(organization));
        when(repository.existsByOrganizationCodeAndDeletedAtIsNull("ORG-02")).thenReturn(true);
        var request = new OrganizationRequest("ORG-02", "Acme", null, null, null, null, null, null);
        assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteSoftDeletesOrganization() {
        Organization organization = Organization.builder().id(1L).organizationCode("ORG-01")
                .organizationName("Acme").status(OrganizationStatus.ACTIVE).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(organization));
        service.delete(1L, 7L);
        assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.DELETED);
        assertThat(organization.getDeletedAt()).isNotNull();
        assertThat(organization.getUpdatedBy()).isEqualTo(7L);
        verify(repository).save(organization);
    }
}
