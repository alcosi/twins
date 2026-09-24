package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinkedTwinByField;

@Component
@Featurer(id = FeaturerTwins.ID_2337,
        name = "Field from item output head twin linked twin field",
        description = "")
@Slf4j
public class FillerFieldFromItemOutputHeadTwinLinkedTwinField extends FillerFieldFromItemOutputLinked {
    @Override
    protected FieldLookuperLinkedTwinByField lookuper() {
        return fieldLookupers.getFromItemOutputHeadTwinLinkedTwinFields();
    }
}
