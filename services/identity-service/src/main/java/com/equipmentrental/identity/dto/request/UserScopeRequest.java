package com.equipmentrental.identity.dto.request;

import java.util.Set;

public record UserScopeRequest(Long organizationId, Set<Long> branchIds, Long customerId) {}
