package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorListShared;
import org.twins.core.service.EntitySmartService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1309,
        name = "FieldTyperSharedSelectInHead",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInHead extends FieldTyperList {
    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        dataListService.findEntitySafe(listId);
        return new FieldDescriptorListShared()
                .setMultiple(false); //todo get from properties
    }

    @Override
    public UUID checkOptionAllowed(TwinFieldEntity twinFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        if (dataListOptionRepository.findByDataListIdAndNotUsedInHead(dataListOptionEntity.getDataListId(), twinFieldEntity.twinClassFieldId(), twinFieldEntity.twin().getHeadTwinId())
                .stream().noneMatch(o -> o.getId().equals(dataListOptionEntity.getId())))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE, twinFieldEntity.twinClassField().logShort() + " can not be filled with optionId[" + dataListOptionEntity.getId() + "] cause it is already in use in headTwin");
        return super.checkOptionAllowed(twinFieldEntity, dataListOptionEntity);
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }

    public DataListEntity getDataListWithValidOption(TwinClassFieldEntity twinClassFieldEntity, UUID headTwinId) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams(), new HashMap<>());
        UUID listId = listUUID.extract(properties);
        DataListEntity dataListEntity = dataListService.findEntitySafe(listId);
        return dataListEntity.setOptions(
                dataListOptionRepository.findByDataListIdAndNotUsedInHead(listId, twinClassFieldEntity.getId(), headTwinId));
    }
}
