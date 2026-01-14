package org.twins.core.domain.factory;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryResultUncommited {
    Kit<TwinCreate, UUID> creates = new Kit<>(TwinCreate::getTwinId);
    Kit<TwinUpdate, UUID> updates = new Kit<>(TwinUpdate::getTwinId);
    Kit<TwinDelete, UUID> deletes = new Kit<>(TwinDelete::getTwinId);
    boolean committable = true;
    Map<UUID, UUID> afterCommitFactories = new HashMap<>(); //todo add support in draft

    public FactoryResultUncommited addOperation(TwinOperation twinOperation) throws ServiceException {
        if (twinOperation instanceof TwinCreate) {
            if (twinOperation.getTwinEntity().getId() == null) {
                twinOperation.getTwinEntity().setId(UuidCreator.getTimeOrdered());
            }
            creates.add((TwinCreate) twinOperation);
        } else if (twinOperation instanceof TwinUpdate) {
            updates.add((TwinUpdate) twinOperation);
        } else if (twinOperation instanceof TwinDelete) {
            deletes.add((TwinDelete) twinOperation);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, twinOperation + " unknown twin operation");
        return this;
    }

    public FactoryResultUncommited join(FactoryResultUncommited joinResult) {
        creates.addAll(joinResult.getCreates());
        updates.addAll(joinResult.getUpdates());
        deletes.addAll(joinResult.getDeletes());
        committable = committable && joinResult.committable;
        return this;
    }

    public FactoryResultUncommited addAfterCommitFactory(UUID twinId, UUID factoryId) {
        afterCommitFactories.put(twinId, factoryId);
        return this;
    }

    public boolean isBlank() {
        return creates.isEmpty() && updates.isEmpty() && deletes.isEmpty();
    }
}
