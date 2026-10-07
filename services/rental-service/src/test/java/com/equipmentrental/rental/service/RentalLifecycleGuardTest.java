package com.equipmentrental.rental.service;

import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.request.CancelOrderRequest;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class RentalLifecycleGuardTest {
    @Test void cannotReleaseEquipmentByCancellingOrderWithAnUnresolvedContract() {
        var orders = mock(RentalOrderRepository.class); var contracts = mock(RentalContractRepository.class);
        var inventory = mock(InventoryClient.class); var order = new RentalOrder();
        order.setStatus(OrderStatus.CONFIRMED); order.setInventoryReservationId("7");
        when(orders.findById(1L)).thenReturn(Optional.of(order));
        when(contracts.existsByRentalOrderIdAndStatusNotIn(eq(1L), anyList())).thenReturn(true);
        var service = new RentalWorkflowService(mock(RentalRequestRepository.class), mock(QuotationRepository.class), orders,
                mock(PricingService.class), mock(RentalDataScopeGuard.class), inventory, contracts);
        assertThrows(ApiException.class, () -> service.cancelOrder(1L, new CancelOrderRequest("Demo")));
        verify(inventory, never()).releaseReservation(anyString(), anyString());
        verify(orders, never()).save(any());
    }
    @Test void cannotSignAppendixOfCancelledContract() {
        var contracts = mock(RentalContractRepository.class); var appendices = mock(ContractAppendixRepository.class);
        var inventory = mock(InventoryClient.class); var contract = new RentalContract(); contract.setStatus(ContractStatus.CANCELLED);
        var appendix = new ContractAppendix(); appendix.setContractId(1L); appendix.setStatus(AppendixStatus.APPROVED);
        when(appendices.findById(2L)).thenReturn(Optional.of(appendix));
        when(contracts.findById(1L)).thenReturn(Optional.of(contract));
        var service = new ContractService(contracts, appendices, mock(RentalOrderRepository.class), mock(RentalDataScopeGuard.class), inventory);
        assertThrows(ApiException.class, () -> service.signAppendix(2L));
        verify(appendices, never()).save(any()); verifyNoInteractions(inventory);
    }
}
