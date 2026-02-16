package org.twins.core.featurer.fieldtyper;


import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStoragePointedHead;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_1349,
        name = "Pointed field from head",
        description = "")
public class FieldTyperPointedHead extends FieldTyperImmutable<FieldDescriptor, FieldValue, TwinFieldStoragePointedHead, TwinFieldSearchNotImplemented> {
    @FeaturerParam(name = "Head twin field", description = "", order = 3)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("headTwinClassFieldId");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public Class<FieldValue> getValueType(TwinClassFieldEntity twinClassField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassField.getFieldTyperParams());
        var headTwinClassField = getHeadTwinClassFieldSafe(properties);
        var headFieldTyper = featurerService.getFeaturer(headTwinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        return headFieldTyper.getValueType(headTwinClassField); //perhaps we should some wrapper here to indicate that the current value is just a pointer
    }

    @Override
    protected FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        var headTwinClassField = getHeadTwinClassFieldSafe(properties);
        var headFieldTyper = featurerService.getFeaturer(headTwinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        // this is recursion safe, even if some head-pointed field points on another head-pointed field, this will just head of head up ()
        var fieldDescriptor = headFieldTyper.getFieldDescriptor(headTwinClassField);
        //todo set editable false
        return fieldDescriptor;
    }

    @Override
    protected FieldValue deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var headTwinClassField = getHeadTwinClassFieldSafe(properties);
        var headTwinFieldValue = twinField.getTwin().getHeadTwin().getFieldValuesKit().get(headTwinClassField.getId());
        return headTwinFieldValue.clone(twinField.getTwinClassField());
    }

    protected TwinClassFieldEntity getHeadTwinClassFieldSafe(Properties properties) throws ServiceException {
        return twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
    }
}
