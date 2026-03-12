package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamStringTwinsEditorType;

import java.util.Properties;

@Log4j2
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1336,
        name = "Text non indexed field",
        description = "")
public class FieldTyperTextNonIndexedField extends FieldTyperSimpleNonIndexed<FieldDescriptorText, FieldValueText, TwinFieldValueSearchNotImplemented> {
    @FeaturerParam(name = "Regexp", description = "", optional = true, defaultValue = "(?s).*", order = 1)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");
    @FeaturerParam(name = "EditorType", description = "", order = 2, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType editorType = new FeaturerParamStringTwinsEditorType("editorType");

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        FieldDescriptorText descriptorText = new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties));
        return descriptorText;
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleNonIndexedEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleNonIndexedEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ?
                        twinFieldEntity.getValue() : null);
    }
}
