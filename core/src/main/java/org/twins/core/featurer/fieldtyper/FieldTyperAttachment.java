package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.attachment.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;

import java.util.*;

import static org.twins.core.dao.attachment.AttachmentFileCreateUpdateProblem.*;
import static org.twins.core.dao.attachment.AttachmentGlobalCreateDeleteProblem.MAX_COUNT_EXCEEDED;
import static org.twins.core.dao.attachment.AttachmentGlobalCreateDeleteProblem.MIN_COUNT_NOT_REACHED;

@Component
@Featurer(id = FeaturerTwins.ID_1316,
        name = "Attachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachment extends FieldTyper<FieldDescriptorAttachment, FieldValueInvisible, TwinAttachmentEntity, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "Min count", description = "Min count of attachments to field", order = 1)
    public static final FeaturerParamInt minCount = new FeaturerParamInt("minCount");
    @FeaturerParam(name = "Max count", description = "Max count of attachments to field", order = 2)
    public static final FeaturerParamInt maxCount = new FeaturerParamInt("maxCount");
    @FeaturerParam(name = "File size MB limit", description = "Max size per file for attachment", order = 3)
    public static final FeaturerParamInt fileSizeMbLimit = new FeaturerParamInt("fileSizeMbLimit");
    @FeaturerParam(name = "File extension list", description = "Allowed extensions for attachment(ex: jpg,jpeg,png)", order = 4)
    public static final FeaturerParamString fileExtensionList = new FeaturerParamString("fileExtensionList");
    @FeaturerParam(name = "File name regexp", description = "File name must match this pattern", order = 5)
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

    public void validate(TwinAttachmentEntity entity, AttachmentCUDValidateResult result, int totalCount, boolean alreadyExists) throws ServiceException {
        FieldDescriptorAttachment descriptor = getFieldDescriptor(entity.getTwinClassField());

        int minCountVal = descriptor.minCount();
        int maxCountVal = descriptor.maxCount();
        long fileSizeMbLimitVal = descriptor.fileSizeMbLimit();
        String fileNameRegexpVal = descriptor.filenameRegExp();
        String fileName = entity.getTitle();
        String fileExtension = getFileExtension(fileName).toLowerCase();
        List<String> allowedExtensions = descriptor.extensions();

        if (minCountVal > 0 && minCountVal > totalCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MIN_COUNT_NOT_REACHED));
        }

        if (maxCountVal > 0 && maxCountVal < totalCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MAX_COUNT_EXCEEDED));
        }

        if (fileSizeMbLimitVal > 0 && entity.getSize() > fileSizeMbLimitVal) {
            if (alreadyExists) {
                result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setProblem(INVALID_SIZE));
            } else {
                result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setProblem(INVALID_SIZE));
            }
        }

        if (!allowedExtensions.contains(fileExtension)) {
            if (alreadyExists) {
                result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setProblem(INVALID_TYPE));
            } else {
                result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setProblem(INVALID_TYPE));
            }
        }

        if (!fileName.matches(fileNameRegexpVal)) {
            if (alreadyExists) {
                result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setProblem(INVALID_NAME));
            } else {
                result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setProblem(INVALID_NAME));
            }
        }
    }


    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }
}
