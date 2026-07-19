package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
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
@Featurer(id = FeaturerTwins.ID_2341,
        name = "Field from featurer params",
        description = "")
@Slf4j
public class FillerFieldFromParams extends Filler {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Value", description = "", order = 2)
    public static final FeaturerParamString value = new FeaturerParamString("value");

    @Autowired
    private TwinService twinService;

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        // fieldId + value are step-constant -> resolve the field entity once (avoids per-item findEntitySafe)
        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        String resolvedValue = value.extract(properties);
        for (FactoryItem factoryItem : factoryItems) {
            factoryItem.getOutput().addField(twinService.createFieldValue(fieldEntity, resolvedValue));
        }
    }
}
