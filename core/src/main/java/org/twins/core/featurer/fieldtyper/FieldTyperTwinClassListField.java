package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldTwinClassListEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassList;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1334,
        name = "Twin class list field",
        description = "Field typer for twin class list field")
public class FieldTyperTwinClassListField extends FieldTyperTwinClassList<FieldDescriptorTwinClassList, FieldValueTwinClassList, TwinFieldSearchNotImplemented> {

    private final TwinClassService twinClassService;

    @Override
    protected FieldDescriptorTwinClassList getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorTwinClassList();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldTwinClassListEntity twinFieldTwinClassListEntity, FieldValueTwinClassList value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldTwinClassListEntity.getTwinClassField().getRequired() && !value.isFilled()) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    twinFieldTwinClassListEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required"
            );
        }

        List<UUID> valueUUIDList = value.getTwinClassList().stream().map(TwinClassEntity::getId).toList();
        if (!twinClassService.allExist(valueUUIDList)) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN,
                    "Not all twin class ids are existing"
            );
        }

        detectValueChange(twinFieldTwinClassListEntity, twinChangesCollector, value.getTwinClassList());
    }

    @Override
    protected FieldValueTwinClassList deserializeValue(Properties properties, TwinField twinField, TwinFieldTwinClassListEntity twinFieldTwinClassListEntity) throws ServiceException {
        return new FieldValueTwinClassList(twinField.getTwinClassField())
                .setTwinClassList(twinFieldTwinClassListEntity != null ? twinFieldTwinClassListEntity.getValue() : null);
    }

//    @Override
//    public Specification<TwinEntity> searchBy(TwinFieldSearchBoolean twinFieldSearchBoolean) {
//        return Specification.where(TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean));
//    }
}
