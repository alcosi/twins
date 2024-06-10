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
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = 2323,
        name = "FillerFieldFromContext",
        description = "")
@Slf4j
public class FillerFieldFromContext extends Filler {
    @FeaturerParam(name = "srcTwinClassFieldId", description = "")
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Lazy
    @Autowired
    TwinService twinService;


    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        fill(properties, factoryItem, templateTwin, FieldLookupMode.fromContextFieldsAndContextTwinFields);
    }

    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldLookupMode fieldLookupMode) throws ServiceException {
        FieldValue fieldValue = factoryService.lookupFieldValue(factoryItem, srcTwinClassFieldId.extract(properties), fieldLookupMode);
        FieldValue clone = twinService.copyToField(fieldValue, dstTwinClassFieldId.extract(properties));
        factoryItem.getOutput().addField(clone);
    }
}
