package org.twins.core.dao;

import java.util.UUID;

public record QuotaKey(UUID twinClassSchemaSpaceId, UUID twinClassId) {}
