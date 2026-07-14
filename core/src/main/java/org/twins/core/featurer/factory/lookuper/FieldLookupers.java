package org.twins.core.featurer.factory.lookuper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Getter
public class FieldLookupers {
    @Lazy
    @Autowired
    private TwinService twinService;
    private final FieldLookuperFromContextFields fromContextFields;
    private final FieldLookuperFromContextFieldsAndContextTwinDbFields fromContextFieldsAndContextTwinDbFields;
    private final FieldLookuperFromContextTwinDbFields fromContextTwinDbFields;
    private final FieldLookuperFromContextTwinLinkedTwinByLinkDbFields fromContextTwinLinkedByLinkTwinFields;
    private final FieldLookuperFromContextTwinLinkedTwinByFieldDbFields fromContextTwinLinkedByFieldTwinFields;
    private final FieldLookuperFromContextTwinHeadTwinDbFields fromContextTwinHeadTwinDbFields;
    private final FieldLookuperFromContextTwinUncommitedFields fromContextTwinUncommitedFields;
    private final FieldLookuperFromItemOutputDbFields fromItemOutputDbFields;
    private final FieldLookuperFromItemOutputUncommitedFields fromItemOutputUncommitedFields;
    private final FieldLookuperFromItemOutputFields fromItemOutputFields;
    private final FieldLookuperFromItemOutputHeadTwinFields fromItemOutputHeadTwinFields;
    private final FieldLookuperFromItemOutputLinkedTwinFields fromItemOutputLinkedTwinFields;
    private final FieldLookuperFromItemOutputHeadTwinLinkedTwinFields fromItemOutputHeadTwinLinkedTwinFields;
    private final FieldLookuperFromItemOutputLinkedTwinHeadTwinFields fromItemOutputLinkedTwinHeadTwinFields;

    /**
     * Bulk-preload DB field values for the CONTEXT twins of all factory items, so that DbFields
     * lookupers reading context-twin fields hit memory instead of issuing a per-item query (N+1
     * elimination). Call from a filler's bulk {@code fill} before {@code fillEach}. Idempotent.
     */
    public void preloadContextTwinsFields(Collection<FactoryItem> items) throws ServiceException {
        Set<TwinEntity> twins = new HashSet<>();
        for (FactoryItem item : items) {
            for (FactoryItem contextItem : item.getContextFactoryItemList()) {
                TwinEntity contextTwin = contextItem.getTwin();
                if (contextTwin != null && contextTwin.getId() != null) {
                    twins.add(contextTwin);
                }
            }
        }
        loadTwinFields(twins);
    }

    /**
     * Bulk-preload DB field values for the OUTPUT twins of all factory items (updates only — a
     * TwinCreate output has no DB row yet). Use for fillers that read item-output DB fields.
     */
    public void preloadOutputTwinsFields(Collection<FactoryItem> items) throws ServiceException {
        Set<TwinEntity> twins = new HashSet<>();
        for (FactoryItem item : items) {
            if (item.getOutput() instanceof TwinUpdate update && update.getTwinEntity() != null && update.getTwinEntity().getId() != null) {
                twins.add(update.getTwinEntity());
            }
        }
        loadTwinFields(twins);
    }

    private void loadTwinFields(Set<TwinEntity> twins) throws ServiceException {
        if (!twins.isEmpty()) {
            twinService.loadTwinFields(twins);
        }
    }
}
