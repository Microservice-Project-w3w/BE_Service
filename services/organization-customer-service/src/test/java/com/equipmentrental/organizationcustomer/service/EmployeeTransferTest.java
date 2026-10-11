package com.equipmentrental.organizationcustomer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.equipmentrental.organizationcustomer.dto.request.EmployeeBranchAssignmentRequest;
import com.equipmentrental.organizationcustomer.entity.Branch;
import com.equipmentrental.organizationcustomer.entity.EmployeeBranchAssignment;
import com.equipmentrental.organizationcustomer.enums.AssignmentStatus;
import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import com.equipmentrental.organizationcustomer.exception.BadRequestException;
import com.equipmentrental.organizationcustomer.repository.EmployeeBranchAssignmentRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmployeeTransferTest {
    private final EmployeeBranchAssignmentRepository repository = mock(EmployeeBranchAssignmentRepository.class);
    private final OrganizationService organizations = mock(OrganizationService.class);
    private final EmployeeService employees = mock(EmployeeService.class);
    private final BranchService branches = mock(BranchService.class);
    private final EmployeeBranchAssignmentService service = new EmployeeBranchAssignmentService(repository, organizations, employees, branches);

    private EmployeeBranchAssignmentRequest request() {
        return new EmployeeBranchAssignmentRequest(7L, 3L, true, LocalDate.now(), null, AssignmentStatus.ACTIVE, null);
    }

    @Test
    void invalidDestinationDoesNotDeactivateCurrentAssignment() {
        when(branches.getEntity(1L, 3L)).thenThrow(new BadRequestException("different organization"));
        assertThrows(BadRequestException.class, () -> service.transfer(1L, request()));
        verifyNoInteractions(repository);
    }

    @Test
    void inactiveDestinationDoesNotDeactivateCurrentAssignment() {
        when(branches.getEntity(1L, 3L)).thenReturn(Branch.builder().status(BranchStatus.INACTIVE).build());
        assertThrows(BadRequestException.class, () -> service.transfer(1L, request()));
        verifyNoInteractions(repository);
    }

    @Test
    void transfersAndReactivatesAnExistingDestinationInOneTransaction() {
        when(branches.getEntity(1L, 3L)).thenReturn(Branch.builder().status(BranchStatus.ACTIVE).build());
        var old = EmployeeBranchAssignment.builder().id(10L).organizationId(1L).employeeId(7L).branchId(2L)
                .status(AssignmentStatus.ACTIVE).primaryAssignment(true).build();
        var destination = EmployeeBranchAssignment.builder().id(11L).organizationId(1L).employeeId(7L).branchId(3L)
                .status(AssignmentStatus.INACTIVE).primaryAssignment(false).assignedTo(LocalDate.now().minusDays(1)).build();
        when(repository.findForTransfer(1L, 7L)).thenReturn(List.of(old, destination));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.transfer(1L, request());
        assertEquals(11L, response.id());
        assertEquals(AssignmentStatus.INACTIVE, old.getStatus());
        assertFalse(old.getPrimaryAssignment());
        assertEquals(AssignmentStatus.ACTIVE, destination.getStatus());
        assertTrue(destination.getPrimaryAssignment());
        assertNull(destination.getAssignedTo());
        verify(repository).save(destination);
    }

    @Test
    void rejectsFutureTransfersBeforeChangingAssignments() {
        when(branches.getEntity(1L, 3L)).thenReturn(Branch.builder().status(BranchStatus.ACTIVE).build());
        var future = new EmployeeBranchAssignmentRequest(7L, 3L, true, LocalDate.now().plusDays(1), null, AssignmentStatus.ACTIVE, null);
        assertThrows(BadRequestException.class, () -> service.transfer(1L, future));
        verifyNoInteractions(repository);
    }
}
