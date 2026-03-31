package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;

import java.util.Properties;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1316,
        name = "Attachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachmentInvisible extends FieldTyperAttachment<FieldValueInvisible> {
    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueInvisible value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    public boolean canSerialize(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return false;
    }

    @Deprecated
    @Override
    protected FieldValueInvisible deserializeValue(Properties properties, TwinField twinField) {
        return new FieldValueInvisible(twinField.getTwinClassField());
    }
}