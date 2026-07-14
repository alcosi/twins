package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListOptionId;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2356,
        name = "Basics flavor data list option",
        description = "Sets output twin flavor_data_list_option_id from configured data list option id"
)
@Slf4j
public class FillerBasicsFlavorDataListOption extends Filler {

    @FeaturerParam(name = "Data list option id", description = "", order = 1)
    public static final FeaturerParamUUID dataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("dataListOptionId");

    @Lazy
    @Autowired
    DataListOptionService dataListOptionService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        fillEach(properties, factoryItems, templateTwin, optional);
    }

    @Override
    protected void fillItem(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID optionId = dataListOptionId.extract(properties);
        DataListOptionEntity option = dataListOptionService.findEntitySafe(optionId);
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        outputTwinEntity.setFlavorDataListOptionId(option.getId());
        log.info("{} flavor will be set to dataListOption[{}]", outputTwinEntity.logShort(), option.getId());
    }
}
