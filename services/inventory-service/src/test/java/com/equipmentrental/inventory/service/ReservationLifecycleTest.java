package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.entity.*;
import com.equipmentrental.inventory.enums.*;
import com.equipmentrental.inventory.repository.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationLifecycleTest {
    EquipmentRepository equipment = mock(EquipmentRepository.class);
    EquipmentModelRepository models = mock(EquipmentModelRepository.class);
    EquipmentReservationItemRepository items = mock(EquipmentReservationItemRepository.class);
    EquipmentReservationRepository reservations = mock(EquipmentReservationRepository.class);
    LocalDateTime oldEnd = LocalDateTime.now().plusDays(2);

    @Test void availabilityExcludesOverlappingHeldOrConfirmedEquipment() {
        when(models.findByOrganizationIdAndEquipmentTypeIdAndActiveTrue(1L, 3L)).thenReturn(List.of(EquipmentModel.builder().id(5L).build()));
        when(equipment.findByOrganizationIdAndBranchIdAndModelIdIn(1L, 1L, List.of(5L))).thenReturn(List.of(
                Equipment.builder().id(7L).status(EquipmentStatus.AVAILABLE).warehouseId(1L).build(),
                Equipment.builder().id(8L).status(EquipmentStatus.AVAILABLE).warehouseId(1L).build()));
        when(items.findBlockingItems(eq(1L), eq(1L), eq(3L), anyList(), anyList(), any(), any(), any()))
                .thenReturn(List.of(EquipmentReservationItem.builder().equipmentId(7L).build()));
        var result = new AvailabilityService(equipment, models, items).checkAvailability(1L, 1L, 3L, LocalDateTime.now(), oldEnd, 2);
        assertEquals(List.of(8L), result.getEquipmentIds()); assertEquals(1, result.getAvailableQuantity()); assertFalse(result.getAvailable());
    }
    EquipmentReservation reservation() {
        var reservation = EquipmentReservation.builder().id(1L).organizationId(1L).branchId(1L).endAt(oldEnd).status(ReservationStatus.CONFIRMED).build();
        when(reservations.findById(1L)).thenReturn(Optional.of(reservation));
        when(equipment.lockForReservation(7L, 1L)).thenReturn(Optional.of(Equipment.builder().id(7L).build()));
        when(items.findByReservationId(1L)).thenReturn(List.of(EquipmentReservationItem.builder().equipmentId(7L).equipmentTypeId(3L).reservationId(1L).endAt(oldEnd).build()));
        return reservation;
    }
    @Test void extensionUpdatesReservationAndEveryItem() {
        var reservation = reservation(); var newEnd = oldEnd.plusDays(3);
        var service = new InternalReservationService(reservations, items, equipment, models);
        service.extend(1L, newEnd);
        assertEquals(newEnd, reservation.getEndAt()); assertEquals(newEnd, items.findByReservationId(1L).get(0).getEndAt());
        verify(reservations).save(reservation); verify(items).saveAll(anyList());
    }
    @Test void extensionRejectsConflictingReservationWithoutSaving() {
        reservation();
        when(items.findBlockingItems(anyLong(), anyLong(), anyLong(), anyList(), anyList(), any(), any(), any()))
                .thenReturn(List.of(EquipmentReservationItem.builder().equipmentId(7L).reservationId(2L).build()));
        assertThrows(IllegalStateException.class, () -> new InternalReservationService(reservations, items, equipment, models).extend(1L, oldEnd.plusDays(3)));
        verify(reservations, never()).save(any());
    }
    @Test void retryOfSameExtensionDoesNotWriteAgain() {
        reservation(); new InternalReservationService(reservations, items, equipment, models).extend(1L, oldEnd);
        verify(reservations, never()).save(any());
    }
}
