package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

@Data
@Accessors(chain = true)
public class TwinTransitionPerform {
    private TwinflowTransitionEntity transitionEntity;
    private TwinUpdate twinUpdate;
}
