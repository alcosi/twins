package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class FactoryResultUncommited {
    List<TwinCreate> creates = new ArrayList<>();
    List<TwinUpdate> updates = new ArrayList<>();
    List<TwinDelete> deletes = new ArrayList<>();
    boolean committable = true;

    public FactoryResultUncommited addOperation(TwinOperation twinOperation) throws ServiceException {
        if (twinOperation instanceof TwinCreate) {
            creates.add((TwinCreate) twinOperation);
        } else if (twinOperation instanceof TwinUpdate) {
            updates.add((TwinUpdate) twinOperation);
        } else if (twinOperation instanceof TwinDelete) {
            deletes.add((TwinDelete) twinOperation);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, twinOperation + " unknown twin operation");
        return this;
    }
}
