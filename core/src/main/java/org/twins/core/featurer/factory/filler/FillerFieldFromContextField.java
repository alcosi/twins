package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;

@Component
@Featurer(id = FeaturerTwins.ID_2312,
        name = "Field from context field",
        description = "")
@Slf4j
public class FillerFieldFromContextField extends FillerFieldFromContext {
    @Override
    public FieldLookuperNearest getLookuper() throws ServiceException {
        return fieldLookupers.getFromContextFields();
    }
}
