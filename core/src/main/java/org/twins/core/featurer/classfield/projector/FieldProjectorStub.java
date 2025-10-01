package org.twins.core.featurer.classfield.projector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4401,
        name = "Field projection stub",
        description = "")
public class FieldProjectorStub extends FieldProjector {

    @Override
    protected void project(Properties properties) throws ServiceException {
    }
}
