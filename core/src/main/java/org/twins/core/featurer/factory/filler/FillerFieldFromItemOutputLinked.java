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
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinked;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2335,
        name = "Field from item output linked",
        description = "Reads a field of the twin linked to the output twin (by the src link field) and copies it to the dst field. "
                + "The navigation source is the required fieldLookuper param (linked twin / head twin's linked twin / linked twin's head twin)")
@Slf4j
public class FillerFieldFromItemOutputLinked extends FillerFieldLookupLinked {
    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassFieldService twinClassFieldService;

    @FeaturerParam(name = "linkedTwinByTwinClassFieldId", description = "")
    public static final FeaturerParamUUID linkedTwinByTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "lookupTwinClassFieldId", description = "")
    public static final FeaturerParamUUID lookupTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("lookupTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    /**
     * Required: the three merged sources have no natural default, and the param's null-fallback
     * (fromContextFields) is a nearest-family lookuper that cannot serve the linked navigation.
     */
    @FeaturerParam(name = "Field lookuper", description = "Navigation source of the linked twin", order = 99)
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Override
    protected FieldLookuperLinked lookuper(Properties properties) {
        return (FieldLookuperLinked) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
    }

    @Override
    protected UUID linkedById(Properties properties) throws ServiceException {
        return linkedTwinByTwinClassFieldId.extract(properties);
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return lookupTwinClassFieldId.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException {
        fieldValue.assertIsDefined(fieldValue.getTwinClassField().logNormal() + " is not found by fieldLookuper");
        UUID extractedDstTwinClassFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue clone = twinService.copyToField(fieldValue, extractedDstTwinClassFieldId);
        if (twinClassFieldService.isInvalidForClass(factoryItem.getOutput().getTwinEntity().getTwinClass(), clone.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Incorrect dstTwinClassFieldId[" + extractedDstTwinClassFieldId + "]");
        factoryItem.getOutput().addField(clone);
    }
}
