package org.twins.core.featurer.factory.lookuper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class FieldLookuper {
    @Autowired
    protected TwinService twinService;

    @Autowired
    protected TwinClassFieldService twinClassFieldService;

    public FieldValue getFreshestValue(TwinEntity twinEntity, UUID twinClassFieldId, FactoryContext factoryContext, String onExceptionMsg) throws ServiceException {
        FactoryItem factoryItem = factoryContext.getFactoryItem(twinEntity.getId());
        FieldValue fieldValue = null;
        if (factoryItem != null) {
            fieldValue = factoryItem.getOutput().getField(twinClassFieldId); // first we will try to get uncommited field
            if (fieldValue == null) {
                fieldValue = getValueFromOutputLinks(twinClassFieldId, factoryItem.getOutput());
            }
        }
        if (fieldValue == null) {
            fieldValue = twinService.getTwinFieldValue(twinEntity, twinClassFieldId);
            if (fieldValue == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, onExceptionMsg);
        }
        return fieldValue;
    }

    public FieldValue getValueFromOutputLinks(UUID twinClassFieldId, TwinSave twinSave) throws ServiceException {
        if (twinSave instanceof TwinCreate twinCreate) {
            var twinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId);
            var linkId = twinClassFieldService.getConfiguredLink(twinClassField);
            if (linkId != null) {
                List<TwinLinkEntity> matchedLinks = twinCreate.getLinksEntityList().stream()
                        .filter(twinLink -> linkId.equals(twinLink.getLinkId()))
                        .toList();
                if (!matchedLinks.isEmpty()) {
                    return new FieldValueLink(twinClassField).setItems(matchedLinks);
                }
            }
        }
        return null;
    }
}
