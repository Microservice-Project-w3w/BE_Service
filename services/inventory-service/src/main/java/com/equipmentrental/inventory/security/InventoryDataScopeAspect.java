package com.equipmentrental.inventory.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.util.List;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class InventoryDataScopeAspect {
    private final InventoryDataScopeGuard guard;
    public InventoryDataScopeAspect(InventoryDataScopeGuard guard) { this.guard = guard; }

    @Around("execution(public * com.equipmentrental.inventory.service.*.*(..)) && !execution(* *.expireOverdueReservations(..))")
    public Object authorize(ProceedingJoinPoint point) throws Throwable {
        MethodSignature method = (MethodSignature) point.getSignature();
        guard.checkArguments(point.getTarget().getClass().getSimpleName(), method.getParameterNames(), point.getArgs());
        Object result = point.proceed();
        if (result instanceof List<?> list) return list.stream().filter(guard::canRead).toList();
        if (!guard.canRead(result)) throw new AccessDeniedException("Dữ liệu nằm ngoài phạm vi được gán");
        return result;
    }
}
