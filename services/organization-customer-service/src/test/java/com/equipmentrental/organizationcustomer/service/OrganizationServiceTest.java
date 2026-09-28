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
}
