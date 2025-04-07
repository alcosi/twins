package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchBaseUuid;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBaseStatus;
import org.twins.core.featurer.fieldtyper.value.FieldValueBaseStatus;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1324,
        name = "BaseStatus",
        description = "Field typer for base status twin field")
public class FieldTyperBaseStatusField extends FieldTyper<FieldDescriptorBaseStatus, FieldValueBaseStatus, TwinEntity, TwinFieldSearchBaseUuid> {

    @Override
    public FieldDescriptorBaseStatus getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorBaseStatus();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueBaseStatus value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinStatusEntity status = value.getStatus();
        if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.twinStatusId, twin.getTwinStatusId(), status.getId())) {
            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(twin).add(
                        historyService.statusChanged(twin.getTwinStatus(), status));
            }
            twin.setTwinStatus(status);
            twin.setTwinStatusId(status.getId());
        }
        if (value.getTwinClassField().getRequired() && ObjectUtils.isEmpty(status)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    value.getTwinClassField().logShort() + " is required");
        }
    }

    @Override
    protected FieldValueBaseStatus deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueBaseStatus(twinField.getTwinClassField()).setStatus(twin.getTwinStatus());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBaseUuid search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.headTwinId));
    }
}
