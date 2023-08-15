package org.twins.core.featurer.businessaccount.initiator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;


@FeaturerType(id = 11,
        name = "BusinessAccountInitiator",
        description = "")
@Slf4j
public abstract class BusinessAccountInitiator extends Featurer {

}
