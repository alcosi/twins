package org.twins.core.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2451,
        name = "Factory item twin is head of factory input twin",
        description = "True if factory item output twin is the head of one of the factory input twins.")
public class ConditionerFactoryItemTwinIsHeadOfFactoryInputTwin extends Conditioner {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID outputId = factoryItem.getTwin() == null ? null : factoryItem.getTwin().getId();
        if (outputId == null) {
            return false;
        }
        for (TwinEntity inputTwin : factoryItem.getFactoryContext().getInputTwinList()) {
            if (outputId.equals(inputTwin.getHeadTwinId())) {
                return true;
            }
        }
        return false;
    }
}
