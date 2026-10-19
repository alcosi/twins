package org.twins.core.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetDatalistOptionId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2450,
        name = "Factory item twin flavor data list option in",
        description = "True if factory item output twin flavor is one of the given data list option ids.")
public class ConditionerFactoryItemTwinFlavorDataListOptionIn extends Conditioner {

    @FeaturerParam(name = "Flavor data list option ids", description = "Allowed flavor data list option ids", order = 1)
    public static final FeaturerParamUUIDSetDatalistOptionId flavorDataListOptionIds = new FeaturerParamUUIDSetDatalistOptionId("flavorDataListOptionIds");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        Set<UUID> expected = flavorDataListOptionIds.extract(properties);
        UUID actual = factoryItem.getTwin().getFlavorDataListOptionId();
        return actual != null && expected.contains(actual);
    }
}
