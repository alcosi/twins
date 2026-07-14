package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinkedTwinByField;

@Component
@Featurer(id = FeaturerTwins.ID_2336,
        name = "Field from item output linked twin head twin field",
        description = "")
@Slf4j
public class FillerFieldFromItemOutputLinkedTwinHeadTwinField extends FillerFieldFromItemOutputLinked {
    @Override
    public FieldLookuperLinkedTwinByField getLookuper() throws ServiceException {
        return fieldLookupers.getFromItemOutputLinkedTwinHeadTwinFields();
    }
}
