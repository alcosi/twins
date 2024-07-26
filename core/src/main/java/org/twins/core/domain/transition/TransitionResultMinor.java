package org.twins.core.domain.transition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TransitionResultMinor extends TransitionResult {
    private List<TwinEntity> transitionedTwinList;
    private List<TwinEntity> processedTwinList;
    private List<UUID> deletedTwinIdList;

    public TransitionResultMinor addTransitionedTwin(TwinEntity twinEntity) {
        transitionedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(transitionedTwinList, twinEntity);
        return this;
    }

    public TransitionResultMinor addTransitionedTwins(List<TwinEntity> twinEntityList) {
        transitionedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(transitionedTwinList, twinEntityList);
        return this;
    }

    public TransitionResultMinor addProcessedTwin(TwinEntity twinEntity) {
        processedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(processedTwinList, twinEntity);
        return this;
    }

    public TransitionResultMinor addProcessedTwins(List<TwinEntity> twinEntityList) {
        processedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(processedTwinList, twinEntityList);
        return this;
    }
}
