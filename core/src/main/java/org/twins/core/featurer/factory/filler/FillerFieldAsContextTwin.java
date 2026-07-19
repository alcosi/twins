package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2345,
        name = "Field as single context twin",
        description = "Fill link field as single context twin")
@Slf4j
public class FillerFieldAsContextTwin extends Filler {
    @FeaturerParam(name = "Twin class field id", description = "Link field witch need to fill", order = 1)
    public static final FeaturerParamUUID twinClassFieldLinkId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldLinkId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        // the field id is step-constant -> resolve the field entity once (avoids per-item findEntitySafe)
        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldLinkId.extract(properties));
        for (FactoryItem factoryItem : factoryItems) {
            var twin = factoryItem.checkSingleContextTwin();
            factoryItem.getOutput().addField(twinService.createFieldValue(fieldEntity, twin.getId().toString()));
        }
    }
}
