package org.twins.core.featurer.factory.lookuper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Slf4j
public abstract class FieldLookuper {
    @Autowired
    protected TwinService twinService;

    public FieldValue getFreshestValue(TwinEntity twinEntity, UUID twinClassFieldId, FactoryContext factoryContext, String onExceptionMsg) throws ServiceException {
        FactoryItem factoryItem = factoryContext.getFactoryItem(twinEntity.getId());
        FieldValue fieldValue = null;
        if (factoryItem != null) {
            fieldValue = factoryItem.getOutput().getField(twinClassFieldId); // first we will try to get uncommited field
        }
        if (fieldValue == null) {
            fieldValue = twinService.getTwinFieldValue(twinEntity, twinClassFieldId);
            if (fieldValue == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, onExceptionMsg);
        }
        return fieldValue;
    }
}
