package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.DateUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATED_AT;

@Component
@Featurer(id = FeaturerTwins.ID_1325,
        name = "BaseDate",
        description = "Field typer for base date twin field")
public class FieldTyperBaseDateField extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueDate, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        UUID fieldId = twinField.getTwinClassField().getId();
        TwinEntity twin = twinField.getTwin();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATED_AT)) {
            return new FieldValueDate(twinField.getTwinClassField(), DateUtils.DEFAULT_DATE_TIME_PATTERN)
                    .setDate(twin.getCreatedAt().toLocalDateTime());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }
}
