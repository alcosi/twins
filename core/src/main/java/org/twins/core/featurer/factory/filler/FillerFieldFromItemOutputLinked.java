package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinkedTwinByField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class FillerFieldFromItemOutputLinked extends Filler {
    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @FeaturerParam(name = "linkedTwinByTwinClassFieldId", description = "")
    public static final FeaturerParamUUID linkedTwinByTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "lookupTwinClassFieldId", description = "")
    public static final FeaturerParamUUID lookupTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("lookupTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    protected void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldLookuperLinkedTwinByField fieldLookuperLinkedTwinByField) throws ServiceException {
        UUID extractedDstTwinClassFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue fieldValue = fieldLookuperLinkedTwinByField.lookupFieldValue(factoryItem, linkedTwinByTwinClassFieldId.extract(properties), lookupTwinClassFieldId.extract(properties));
        FieldValue clone = twinService.copyToField(fieldValue, extractedDstTwinClassFieldId);
        if (!twinClassService.isInstanceOf(factoryItem.getOutput().getTwinEntity().getTwinClass(), clone.getTwinClassField().getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Incorrect dstTwinClassFieldId[" + extractedDstTwinClassFieldId +"]");
        factoryItem.getOutput().addField(clone);
    }
}
