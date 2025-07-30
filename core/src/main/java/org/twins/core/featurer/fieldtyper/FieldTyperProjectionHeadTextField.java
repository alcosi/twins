package org.twins.core.featurer.fieldtyper;


import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.cambium.common.exception.ErrorCodeCommon.FEATURER_WITHOUT_SERIALIZATION;

@Component
@Featurer(id = FeaturerTwins.ID_1335,
        name = "Projection from head",
        description = "")
public class FieldTyperProjectionHeadTextField extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldSimpleEntity, TwinFieldSearchText> {
    @FeaturerParam(name = "Head field", description = "", order = 3)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("headTwinClassFieldId");

    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;


    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(FEATURER_WITHOUT_SERIALIZATION, "You can't change this field from this " + twin.logNormal() + ". Try to do that in head twin");
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(
                        twinFieldSimpleRepository.findByTwinIdAndTwinClassFieldId(
                                twinField.getTwin().getHeadTwinId(), twinClassFieldId.extract(properties)
                        ).getValue()
                );
    }
}
