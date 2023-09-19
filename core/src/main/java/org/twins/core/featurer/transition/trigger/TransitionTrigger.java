package org.twins.core.featurer.transition.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;


@FeaturerType(id = 15,
        name = "TransitionTrigger",
        description = "")
@Slf4j
public abstract class TransitionTrigger extends Featurer {

}
