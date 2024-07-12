package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;

@Data
@Accessors(chain = true)
public class FactoryResultCommited {
    private List<TwinEntity> createdTwinList;
    private List<TwinEntity> updatedTwinList;
    private List<TwinEntity> deletedTwinList;

    public FactoryResultCommited addUpdatedTwin(TwinEntity twinEntity) {
        updatedTwinList = CollectionUtils.safeAdd(updatedTwinList, twinEntity);
        return this;
    }

    public FactoryResultCommited addUpdatedTwins(List<TwinEntity> twinEntityList) {
        updatedTwinList = CollectionUtils.safeAdd(updatedTwinList, twinEntityList);
        return this;
    }

    public FactoryResultCommited addCreatedTwin(TwinEntity twinEntity) {
        createdTwinList = CollectionUtils.safeAdd(createdTwinList, twinEntity);
        return this;
    }

    public FactoryResultCommited addCreatedTwins(List<TwinEntity> twinEntityList) {
        createdTwinList = CollectionUtils.safeAdd(createdTwinList, twinEntityList);
        return this;
    }
}
