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

import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1334,
        name = "Twin class list field",
        description = "Field typer for twin class list field")
public class FieldTyperTwinClassListField extends FieldTyperTwinClassList<FieldDescriptorTwinClassList, FieldValueTwinClassList, TwinFieldSearchNotImplemented> {

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

        Set<UUID> valueUUIDList = value.getTwinClassIdSet();
        if (!twinClassService.allExist(valueUUIDList)) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN,
                    "Not all twin class ids are existing"
            );
        }

        if (valueUUIDList.contains(twinFieldTwinClassListEntity.getTwin().getTwinClassId())) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_LIST_CYCLE,
                    "You can't put classId[" + twinFieldTwinClassListEntity.getTwin().getTwinClassId() + "] to the class list because of cycle"
            );
        }

        detectValueChange(twinFieldTwinClassListEntity, twinChangesCollector, value.getTwinClassIdSet());
    }

    @Override
    protected FieldValueTwinClassList deserializeValue(Properties properties, TwinField twinField, TwinFieldTwinClassListEntity twinFieldTwinClassListEntity) throws ServiceException {
        return new FieldValueTwinClassList(twinField.getTwinClassField())
                .setTwinClassIdSet(
                        twinFieldTwinClassListEntity != null
                                ? twinFieldTwinClassListEntity.getTwinClassSet().stream().map(TwinClassEntity::getId).collect(Collectors.toSet())
                                : null
                );
    }

//    @Override
//    public Specification<TwinEntity> searchBy(TwinFieldSearchBoolean twinFieldSearchBoolean) {
//        return Specification.where(TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean));
//    }
}
