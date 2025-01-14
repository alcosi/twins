package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public abstract class TwinOperation {
    protected TwinEntity twinEntity; // only for new/updated data

    // can be used to group TwinOperations in Kit
    public UUID getTwinId() {
        return twinEntity != null ? twinEntity.getId() : null;
    }
}
