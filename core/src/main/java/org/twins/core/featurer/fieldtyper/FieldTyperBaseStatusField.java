package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchId;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatusSingle;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1324,
        name = "BaseStatus",
        description = "Field typer for base status twin field")
public class FieldTyperBaseStatusField extends FieldTyper<FieldDescriptorImmutable, FieldValueStatusSingle, TwinEntity, TwinFieldSearchId> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueStatusSingle value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "direct status change is not allowed. Use transition instead");
    }

    @Override
    protected FieldValueStatusSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueStatusSingle(twinField.getTwinClassField()).setStatus(twin.getTwinStatus());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchId search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.twinStatusId));
    }
}
