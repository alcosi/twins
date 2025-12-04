package org.twins.core.featurer.classfield.projector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4412,
        name = "Field projection fetch string to string v1",
        description = "")
public class FieldProjectorFetchStringToStringV1 extends FieldProjector {

    @Override
    protected FieldValue project(Properties properties, TwinClassFieldEntity field, TwinEntity entity) throws ServiceException {
        return null;
    }
}
