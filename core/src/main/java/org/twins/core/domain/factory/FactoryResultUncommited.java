package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.domain.twinoperation.TwinOperation;

import java.util.List;

@Data
@Accessors(chain = true)
public class FactoryResultUncommited {
    List<TwinOperation> operations; //twinCreate, twinsUpdate and twinDelete
    boolean committable = true;

    public FactoryResultUncommited addOperation(TwinOperation twinOperation) {
        operations = CollectionUtils.safeAdd(operations, twinOperation);
        return this;
    }
}
