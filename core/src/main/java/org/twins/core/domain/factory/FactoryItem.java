package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinOperation;

import java.util.List;

@Data
@Accessors(chain = true)
public class FactoryItem {
    private FactoryContext factoryContext;
    private TwinOperation outputTwin;
    private List<TwinEntity> contextTwinList;

    public TwinEntity getContextFirstTwin() {
        if (CollectionUtils.isEmpty(contextTwinList))
            return null;
        return contextTwinList.get(0);
    }
}
