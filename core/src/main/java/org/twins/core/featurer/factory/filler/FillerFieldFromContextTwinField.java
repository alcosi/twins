package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;

import java.util.Collection;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2311,
        name = "Field from context twin field",
        description = "")
@Slf4j
public class FillerFieldFromContextTwinField extends FillerFieldFromContext {
    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        fieldLookupers.preloadContextTwinsFields(factoryItems);
        fillEach(properties, factoryItems, templateTwin, optional);
    }

    @Override
    public FieldLookuperNearest getLookuper() throws ServiceException {
        return fieldLookupers.getFromContextTwinDbFields();
    }
}
