package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringTwinsEditorType;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4603,
        name = "Field Overwriter - Text",
        description = "Overwrite a text field with given params")
public class FieldParamOverwriterText extends FieldParamOverwriter<FieldDescriptorText> {
    @FeaturerParam(name = "Regexp", description = "", order = 1, optional = true)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");
    @FeaturerParam(name = "EditorType", description = "", order = 2, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType editorType = new FeaturerParamStringTwinsEditorType("editorType");
    @FeaturerParam(name = "Unique", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean unique = new FeaturerParamBoolean("unique");

    @Override
    protected FieldDescriptorText getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties));
    }
}
