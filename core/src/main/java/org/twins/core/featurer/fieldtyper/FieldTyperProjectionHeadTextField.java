package org.twins.core.featurer.fieldtyper;


import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringTwinsEditorType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1333,
        name = "Projection text",
        description = "")
public abstract class FieldTyperProjectionHeadTextField extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldSimpleEntity, TwinFieldSearchText> {

    @FeaturerParam(name = "Regexp", description = "", order = 1)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");
    @FeaturerParam(name = "EditorType", description = "", order = 2, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType editorType = new FeaturerParamStringTwinsEditorType("editorType");
    @FeaturerParam(name = "TwinClassFieldId", description = "", order = 3)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("twinClassFieldId");

    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;


    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties));
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
