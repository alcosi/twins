package org.twins.core.dao.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
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
    private TwinEntity twin;
    private UUID twinClassFieldId;
    private TwinClassFieldEntity twinClassField;
    private BigDecimal delta;

    @Override
    public UUID getId() {
        return id;
    }

    public static TwinFieldDecimalIncrement of(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) {
        return new TwinFieldDecimalIncrement()
                .setId(UuidUtils.generate())
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setDelta(null);
    }

}
