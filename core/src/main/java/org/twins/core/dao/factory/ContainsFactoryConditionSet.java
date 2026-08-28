package org.twins.core.dao.factory;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public interface ContainsFactoryConditionSet extends EasyLoggable {
    UUID getId();
    UUID getTwinFactoryConditionSetId();
    Boolean getTwinFactoryConditionInvert();
    TwinFactoryConditionSetEntity getTwinFactoryConditionSet();
    ContainsFactoryConditionSet setTwinFactoryConditionSet(TwinFactoryConditionSetEntity twinFactoryConditionSet);
}
