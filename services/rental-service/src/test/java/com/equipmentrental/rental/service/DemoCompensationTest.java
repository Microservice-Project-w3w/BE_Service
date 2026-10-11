package com.equipmentrental.rental.service;
import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import org.springframework.transaction.support.*;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class DemoCompensationTest {
    @AfterEach void clear() { TransactionSynchronizationManager.clear(); }
    @Test void releasesRemoteHoldAfterLocalRollback() {
        var client=new InventoryClient("http://inventory");var builder=RestClient.builder();
        var server=MockRestServiceServer.bindTo(builder).build();ReflectionTestUtils.setField(client,"restClient",builder.build());
        var journal=mock(DemoSyncJournal.class);ReflectionTestUtils.setField(client,"journal",journal);
        var task=new DemoSyncTask();task.id="task";when(journal.begin(eq(1L),eq(1L),eq(1L),eq("CREATE"),anyString())).thenReturn(task);
        var order=new RentalOrder();ReflectionTestUtils.setField(order,"id",1L);order.setOrganizationId(1L);order.setBranchId(1L);order.setOrderCode("ORDER");order.setStartAt(LocalDateTime.now().plusDays(1));order.setEndAt(LocalDateTime.now().plusDays(2));
        server.expect(requestTo("/internal/reservations")).andRespond(withSuccess("{\"id\":1}",MediaType.APPLICATION_JSON));
        server.expect(requestTo("/internal/demo-reservations/compensate")).andRespond(withSuccess());
        TransactionSynchronizationManager.initSynchronization();assertThat(client.createReservation(order,LocalDateTime.now().plusHours(1),List.of(3L))).isEqualTo("1");
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(journal).update("task","COMPENSATED",null);server.verify();
    }
    @Test void keepsDurablePendingIntentWhenRemoteResultAndCompensationAreUnavailable() {
        var client=new InventoryClient("http://inventory");var builder=RestClient.builder();
        var server=MockRestServiceServer.bindTo(builder).build();ReflectionTestUtils.setField(client,"restClient",builder.build());
        var journal=mock(DemoSyncJournal.class);ReflectionTestUtils.setField(client,"journal",journal);
        var task=new DemoSyncTask();task.id="task";when(journal.begin(anyLong(),anyLong(),anyLong(),anyString(),anyString())).thenReturn(task);
        var order=new RentalOrder();ReflectionTestUtils.setField(order,"id",1L);order.setOrganizationId(1L);order.setBranchId(1L);order.setOrderCode("ORDER");
        server.expect(requestTo("/internal/reservations")).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        server.expect(requestTo("/internal/demo-reservations/compensate")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        TransactionSynchronizationManager.initSynchronization();assertThatThrownBy(()->client.createReservation(order,LocalDateTime.now().plusHours(1),List.of(3L))).isInstanceOf(RuntimeException.class);
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(journal).update(eq("task"),eq("PENDING"),anyString());server.verify();
    }
}
