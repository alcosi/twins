package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorListShared;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1309,
        name = "Select (shared in head)",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInHead extends FieldTyperList {
    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = dataListId.extract(properties);
        dataListService.findEntitySafe(listId);
        return new FieldDescriptorListShared()
                .setMultiple(false); //todo get from properties
    }

    @Override
    public UUID checkOptionAllowed(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        if (!dataListService.findOptionIdsByDataListIdAndNotUsedInHeadExcludingTwin(dataListOptionEntity.getDataListId(), twinClassFieldEntity.getId(), twinEntity.getHeadTwinId(), twinEntity.getId()).contains(dataListOptionEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be filled with optionId[" + dataListOptionEntity.getId() + "] cause it is already in use in headTwin");
        return super.checkOptionAllowed(twinEntity, twinClassFieldEntity, dataListOptionEntity);
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
