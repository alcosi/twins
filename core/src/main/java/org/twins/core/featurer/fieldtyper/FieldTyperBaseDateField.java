package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchDate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBaseDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATED_AT;

@Component
@Featurer(id = FeaturerTwins.ID_1325,
        name = "BaseDate",
        description = "Field typer for base date twin field")
public class FieldTyperBaseDateField extends FieldTyper<FieldDescriptorBaseDate, FieldValueDate, TwinEntity, TwinFieldSearchDate> {

    @Override
    public FieldDescriptorBaseDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorBaseDate();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        //TODO implement if changeble date fields will add twin
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        UUID fieldId = twinField.getTwinClassField().getId();
        TwinEntity twin = twinField.getTwin();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATED_AT)) {
            return new FieldValueDate(twinField.getTwinClassField()).setDate(twin.getCreatedAt().toString());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchDate search) throws ServiceException {
        UUID fieldId = search.getTwinClassFieldEntity().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATED_AT)) {
            return null;
            //TODO adapt checkFieldDate for work with twin-base-fields
//            return Specification.where(TwinSpecification.checkFieldDate(search));
        } else {
            return null;
        }
    }
}
