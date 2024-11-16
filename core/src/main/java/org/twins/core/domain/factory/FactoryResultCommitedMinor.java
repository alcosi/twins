package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FactoryResultCommitedMinor extends FactoryResultCommited {
    private List<TwinEntity> createdTwinList;
    private List<TwinEntity> updatedTwinList;
    private List<UUID> deletedTwinIdList;

    public FactoryResultCommitedMinor addUpdatedTwin(TwinEntity twinEntity) {
        updatedTwinList = CollectionUtils.safeAdd(updatedTwinList, twinEntity);
        return this;
    }

    public FactoryResultCommitedMinor addUpdatedTwins(List<TwinEntity> twinEntityList) {
        updatedTwinList = CollectionUtils.safeAdd(updatedTwinList, twinEntityList);
        return this;
    }

    public FactoryResultCommitedMinor addCreatedTwin(TwinEntity twinEntity) {
        createdTwinList = CollectionUtils.safeAdd(createdTwinList, twinEntity);
        return this;
    }

    public FactoryResultCommitedMinor addCreatedTwins(List<TwinEntity> twinEntityList) {
        createdTwinList = CollectionUtils.safeAdd(createdTwinList, twinEntityList);
        return this;
    }

    public FactoryResultCommitedMinor addDeletedTwin(UUID twinId) {
        deletedTwinIdList = CollectionUtils.safeAdd(deletedTwinIdList, twinId);
        return this;
    }

    public FactoryResultCommitedMinor addDeletedTwins(List<UUID> twinIds) {
        deletedTwinIdList = CollectionUtils.safeAdd(deletedTwinIdList, twinIds);
        return this;
    }
}
