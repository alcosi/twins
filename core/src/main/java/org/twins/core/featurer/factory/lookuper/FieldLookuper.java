package org.twins.core.featurer.factory.lookuper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class FieldLookuper {
    @Autowired
    protected TwinService twinService;

    @Autowired
    protected TwinLinkService twinLinkService;

    @Autowired
    protected TwinClassFieldService twinClassFieldService;

    /**
     * UUID-based convenience for callers that did not resolve the field entity yet — the Linked
     * lookuper family still uses it; prefer the entity variant.
     */
    public FieldValue getFreshestValue(TwinEntity twinEntity, UUID twinClassFieldId, FactoryContext factoryContext) throws ServiceException {
        return getFreshestValue(twinEntity, twinClassFieldService.findEntitySafe(twinClassFieldId), factoryContext);
    }

    /**
     * Resolves the freshest value of the field across uncommitted output, output links and the db.
     * Returns null when nothing is found — the lookuper contract: not-found is reported to the
     * caller (undefined value at the batch boundary), not thrown here.
     */
    public FieldValue getFreshestValue(TwinEntity twinEntity, TwinClassFieldEntity twinClassField, FactoryContext factoryContext) throws ServiceException {
        FactoryItem factoryItem = factoryContext.getFactoryItem(twinEntity.getId());
        FieldValue fieldValue = null;
        if (factoryItem != null) {
            fieldValue = factoryItem.getOutput().getField(twinClassField); // first we will try to get uncommited field
            if (fieldValue == null) {
                fieldValue = getValueFromOutputLinks(twinClassField, factoryItem.getOutput());
            }
        }
        if (fieldValue == null) {
            fieldValue = twinService.getTwinFieldValue(twinEntity, twinClassField);
        }
        return fieldValue;
    }

    /**
     * UUID-based convenience — resolves the field entity (cached service lookup); prefer the entity variant.
     */
    public FieldValue getValueFromOutputLinks(UUID twinClassFieldId, TwinSave twinSave) throws ServiceException {
        return getValueFromOutputLinks(twinClassFieldService.findEntitySafe(twinClassFieldId), twinSave);
    }

    public FieldValue getValueFromOutputLinks(TwinClassFieldEntity twinClassField, TwinSave twinSave) throws ServiceException {
        if (twinSave instanceof TwinCreate twinCreate) {
            var linkId = twinClassFieldService.getConfiguredLink(twinClassField);
            if (linkId != null) {
                List<TwinLinkEntity> matchedLinks = twinCreate.getLinksEntityList().stream()
                        .filter(twinLink -> linkId.equals(twinLink.getLinkId()))
                        .toList();
                if (!matchedLinks.isEmpty()) {
                    // dstTwin is @Transient: batch-internal links arrive with dstTwin already set by
                    // TwinLinkAddTemporalRestDTOReverseMapper (TemporalIdContext registry), the rest are existing
                    // twins — the bulk load covers them and short-circuits on already-loaded entities
                    twinLinkService.loadDstTwin(matchedLinks);
                    return new FieldValueLink(twinClassField).setItems(matchedLinks.stream().map(TwinLinkEntity::getDstTwin).toList());
                }
            }
        }
        return null;
    }
}
