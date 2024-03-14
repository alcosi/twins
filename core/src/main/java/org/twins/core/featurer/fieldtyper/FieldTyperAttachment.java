package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachment;

import java.util.Properties;

@Component
@Featurer(id = 1316,
        name = "FieldTyperAttachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachment extends FieldTyper<FieldDescriptorAttachment, FieldValueAttachment> {

    @FeaturerParam(name = "multiple", description = "Allow add multiple attachments to field")
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");
    @FeaturerParam(name = "fileSizeMbLimit", description = "Max size per file for attachment")
    public static final FeaturerParamInt fileSizeMbLimit = new FeaturerParamInt("fileSizeMbLimit");
    @FeaturerParam(name = "fileExtensionList", description = "Allowed extensions for attachment(ex: jpg,jpeg,png)")
    public static final FeaturerParamString fileExtensionList = new FeaturerParamString("fileExtensionList");
    @FeaturerParam(name = "fileNameRegexp", description = "File name must match this pattern")
    public static final FeaturerParamString fileNameRegexp = new FeaturerParamString("fileNameRegexp");

    @Override
    public FieldDescriptorAttachment getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorAttachment()
                .multiple(multiple.extract(properties));
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueAttachment value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueAttachment deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) {
        return null;
    }
}
