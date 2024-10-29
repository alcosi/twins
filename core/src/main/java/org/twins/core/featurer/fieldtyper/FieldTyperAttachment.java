package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1316,
        name = "FieldTyperAttachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachment extends FieldTyper<FieldDescriptorAttachment, FieldValueInvisible, TwinAttachmentEntity, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "minCount", description = "Min count of attachments to field")
    public static final FeaturerParamInt minCount = new FeaturerParamInt("minCount");
    @FeaturerParam(name = "maxCount", description = "Max count of attachments to field")
    public static final FeaturerParamInt maxCount = new FeaturerParamInt("maxCount");
    @FeaturerParam(name = "fileSizeMbLimit", description = "Max size per file for attachment")
    public static final FeaturerParamInt fileSizeMbLimit = new FeaturerParamInt("fileSizeMbLimit");
    @FeaturerParam(name = "fileExtensionList", description = "Allowed extensions for attachment(ex: jpg,jpeg,png)")
    public static final FeaturerParamString fileExtensionList = new FeaturerParamString("fileExtensionList");
    @FeaturerParam(name = "fileNameRegexp", description = "File name must match this pattern")
    public static final FeaturerParamString fileNameRegexp = new FeaturerParamString("fileNameRegexp");



    @Override
    public FieldDescriptorAttachment getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        String extensions = fileExtensionList.extract(properties);
        return new FieldDescriptorAttachment()
                .minCount(minCount.extract(properties))
                .maxCount(maxCount.extract(properties))
                .fileSizeMbLimit(fileSizeMbLimit.extract(properties))
                .filenameRegExp(fileNameRegexp.extract(properties))
                .extensions(ObjectUtils.isEmpty(extensions) ? new ArrayList<>() : Arrays.asList(extensions.split(",")));
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueInvisible value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Deprecated
    @Override
    protected FieldValueInvisible deserializeValue(Properties properties, TwinField twinField) {
        return new FieldValueInvisible(twinField.getTwinClassField());
    }
}
