package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2365,
        name = "Field from context twin field",
        description = "")
@Slf4j
public class FillerFieldFromContextTwinField extends FillerFieldFromContext {
    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return fieldLookupers.getFromContextTwinFields();
    }
}
