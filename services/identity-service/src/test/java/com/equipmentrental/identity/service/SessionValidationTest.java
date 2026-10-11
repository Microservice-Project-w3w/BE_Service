package com.equipmentrental.identity.service;

import com.equipmentrental.identity.entity.UserSession;
import com.equipmentrental.identity.repository.UserSessionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionValidationTest {
    final UserSessionRepository repository = mock(UserSessionRepository.class);
    final SessionService service = new SessionService(repository, 7);
    @Test void missingOrForeignSessionIsRejected() {
        assertThrows(ResponseStatusException.class, () -> service.requireActiveForUser(1L, 2L));
    }
    @Test void revokedSessionIsRejectedEvenIfJwtHasNotExpired() {
        var session = mock(UserSession.class);
        when(repository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(session));
        when(session.isActive()).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.requireActiveForUser(1L, 2L));
    }
    @Test void validOwnedSessionIsAcceptedWithoutChangingIt() {
        var session = mock(UserSession.class);
        when(repository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(session));
        when(session.isActive()).thenReturn(true);
        assertDoesNotThrow(() -> service.requireActiveForUser(1L, 2L));
        verify(repository, never()).save(any());
    }
}
