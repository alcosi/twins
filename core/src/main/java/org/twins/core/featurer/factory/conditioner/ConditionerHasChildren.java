package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.BasicSearch;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Component
@Featurer(id = 2407,
        name = "ConditionerHasChildren",
        description = "")
@Slf4j
public class ConditionerHasChildren extends Conditioner {
    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSet("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        long count = twinSearchService.count(new BasicSearch()
                .addHeaderTwinId(factoryItem.getOutputTwin().getTwinEntity().getId())
                .addStatusId(statusIds.extract(properties)));
        return count > 0;
    }
}