package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1307,
        name = "FieldTyperSharedSelectInDomain",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInDomain extends FieldTyperList {
    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        dataListService.findEntitySafe(listId);
        return new FieldDescriptorList()
                .supportCustom(false)
                .multiple(false)
                .options(dataListOptionRepository.findByDataListIdAndNotUsedInDomain(listId, twinClassFieldEntity.getId()));
    }

    @Override
    public UUID checkOptionAllowed(TwinFieldEntity twinFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        if (dataListOptionRepository.findByDataListIdAndNotUsedInDomain(dataListOptionEntity.getDataListId(), twinFieldEntity.getTwinClassFieldId()).stream().noneMatch(o -> o.getId().equals(dataListOptionEntity.getId())))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " can not be filled with optionId[" + dataListOptionEntity.getId() + "] cause it is already in use in domain");
        return super.checkOptionAllowed(twinFieldEntity, dataListOptionEntity);
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
