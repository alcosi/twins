package org.twins.core.domain.transition;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

import java.util.*;

@Data
@Accessors(chain = true)
public class TransitionDraftContext {
    private TwinflowTransitionEntity transitionEntity;
    private Map<UUID, TwinEntity> targetTwinList; // key: twinId

    public TransitionDraftContext addTargetTwin(TwinEntity twinEntity) {
        if (targetTwinList == null)
            targetTwinList = new HashMap<>();
        targetTwinList.put(twinEntity.getId(), twinEntity);
        return this;
    }
}
