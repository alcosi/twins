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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2323,
        name = "Field from context",
        description = "")
@Slf4j
public class FillerFieldFromContext extends FillerFieldLookup {
    @FeaturerParam(name = "Src twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "Dst twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @FeaturerParam(name = "Field lookuper", description = "Source of the field value", order = 99, optional = true, defaultValue = "fromContextFieldsAndContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassFieldService twinClassFieldService;

    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return srcTwinClassFieldId.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException {
        UUID extractedDstTwinClassFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue clone = twinService.copyToField(fieldValue, extractedDstTwinClassFieldId);
        if (twinClassFieldService.isInvalidForClass(factoryItem.getOutput().getTwinEntity().getTwinClass(), clone.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Incorrect dstTwinClassFieldId[" + extractedDstTwinClassFieldId + "]");
        factoryItem.getOutput().addField(clone);
    }
}
