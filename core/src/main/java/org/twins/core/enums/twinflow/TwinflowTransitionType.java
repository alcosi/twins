package org.twins.core.enums.twinflow;

import lombok.Getter;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

@Getter
public enum TwinflowTransitionType {
    STATUS_CHANGE,
    OPERATION,
    MARKETING,
    STATUS_CHANGE_MARKETING;

    public static boolean isMarketing(TwinflowTransitionEntity transition) {
        return transition.getTwinflowTransitionTypeId() == MARKETING
                || transition.getTwinflowTransitionTypeId() == STATUS_CHANGE_MARKETING;
    }
}
