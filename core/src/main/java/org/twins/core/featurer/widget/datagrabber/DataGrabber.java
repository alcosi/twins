package org.twins.core.featurer.widget.datagrabber;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;


@FeaturerType(id = 11,
        name = "DataGrabber",
        description = "")
@Slf4j
public abstract class DataGrabber extends Featurer {

}
