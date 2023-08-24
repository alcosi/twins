package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper extends Featurer {

}
