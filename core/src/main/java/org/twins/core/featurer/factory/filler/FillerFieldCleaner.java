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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_2331,
        name = "FillerFieldCleaner",
        description = "")
@Slf4j
public class FillerFieldCleaner extends Filler {
    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID twinClassFieldIdExtracted = twinClassFieldId.extract(properties);
        FieldValue fieldValue = factoryService.lookupFieldValue(factoryItem, twinClassFieldIdExtracted, FieldLookupMode.fromContextTwinFields);
        if(null != fieldValue) {
            fieldValue.nullify();
            factoryItem.checkSingleContextItem().getOutput().addField(fieldValue);
        } else {
            log.info("FieldValue of class[" + twinClassFieldIdExtracted + "] is absent fot twin[" + factoryItem.getTwin() + "] and can not be cleared");
        }
    }
}