package org.twins.core.dao.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.UuidUtils;
import org.twins.core.domain.Identifiable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wrapper class for atomic increment operations on decimal fields.
 * Separate from TwinFieldDecimalEntity to avoid cache pollution issues.
 */
@Data
@Accessors(chain = true)
public class TwinFieldDecimalIncrement implements Identifiable {
    private UUID id;
    private UUID twinId;
    private UUID twinClassFieldId;
    private BigDecimal delta;

    public TwinFieldDecimalIncrement() {
    }

    public TwinFieldDecimalIncrement(UUID twinId, UUID twinClassFieldId, BigDecimal delta) {
        this.twinId = twinId;
        this.twinClassFieldId = twinClassFieldId;
        this.delta = delta;
        this.id = UuidUtils.generate();
    }

    @Override
    public UUID getId() {
        return id;
    }
}
