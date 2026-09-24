package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinkedTwinByField;

@Component
@Featurer(id = FeaturerTwins.ID_2335,
        name = "Field from item output linked twin field",
        description = "")
@Slf4j
public class FillerFieldFromItemOutputLinkedTwinField extends FillerFieldFromItemOutputLinked {
    @Override
    protected FieldLookuperLinkedTwinByField lookuper() {
        return fieldLookupers.getFromItemOutputLinkedTwinFields();
    }
}
