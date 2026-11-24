package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.SystemIdLookup;

import java.util.UUID;
import java.util.function.Function;

/**
 * Twin field VALUE access and groupability — the contract behind twin grouping. Today it is the
 * pre-search groupability check plus the single read point for group keys; the rest of the
 * field-value machinery (parseFieldValue / materializeFieldValues / createFieldValue, still on
 * TwinService) migrates here over time.
 */
@Service
@RequiredArgsConstructor
public class TwinFieldService {
    // createFieldValue until the value plumbing migrates here
    private final TwinService twinService;
    private final FeaturerService featurerService;

    /**
     * Whether the field may serve as a group level — checked before the search runs, so a bad level
     * fails cheaply instead of mid-assembly. A base field needs {@link TwinEntity.BasicField#isGroupable()}
     * (column-backed or kit-backed); a kit-backed one's values deserialize from the twin kit into
     * FieldValueSelect, so it groups exactly like a dynamic select field: extractKeys splits it
     * into per-option keys; dynamic fields need a storage from the same acceptance set as the
     * count/v1 countGroup contract (link, datalist, simple, decimal, boolean, timestamp).
     */
    public void checkGroupable(TwinClassFieldEntity field) throws ServiceException {
        if (SystemIdLookup.isSystemField(field.getId())) {
            TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(field.getId());
            if (basicField == null || !basicField.isGroupable()) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "Field " + field.getId() + " is not groupable");
            }
            return;
        }
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinFieldStorage storage = fieldTyper.getStorage(field);
        if (!(storage instanceof TwinFieldStorageLink
                || storage instanceof TwinFieldStorageDatalist
                || storage instanceof TwinFieldStorageTimestamp
                || storage instanceof TwinFieldStorageBoolean
                || storage instanceof TwinFieldStorageDecimal
                || storage instanceof TwinFieldStorageSimple)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "{} is not countable", field.logNormal());
        }
    }

    /**
     * The current value of one field on one twin — the single read point for twin grouping
     * (TwinGroupLevel.extractKeys). A dynamic field reads twin.fieldValuesKit (filled by
     * TwinService.loadFieldsValues); an absent value comes back as an undefined value of the field's
     * type, never null, so the group key type survives even without a value. A kit-backed base field
     * (tags, markers) delegates to its typer over the twin kit. A column-backed base field lives in
     * a twin column instead: an entity already attached to the twin (EAGER association or a filled
     * transient relation — ownerUser, twinStatus, twinClass, assignerUser, createdByUser, headTwin)
     * becomes the concrete single value at once; a bare id becomes an id-only
     * {@link FieldValueReferenceSingle} for the caller to bulk-materialize
     * (TwinGroupedSearchService.materializeKeyValues).
     */
    public FieldValue getFieldValue(TwinEntity twin, TwinClassFieldEntity field) throws ServiceException {
        TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(field.getId());
        if (basicField == null) {
            FieldValue value = twin.getFieldValuesKit() == null ? null : twin.getFieldValuesKit().get(field.getId());
            return value != null ? value : twinService.createFieldValue(field);
        }
        if (!basicField.isTwinColumn()) {
            // kit-backed base field (tags, markers): the typer deserializes a FieldValueSelect
            // from the (bulk-preloaded) twinTagKit / twinMarkerKit
            return twinService.deserializeValue(twinService.wrapField(twin, field));
        }
        if (!basicField.isGroupable()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " is not groupable");
        }
        switch (basicField) {
            case OWNER_USER_ID:
                return basicKeyValue(field, FieldValueUserSingle::new, twin.getOwnerUser(), twin.getOwnerUserId());
            case ASSIGNEE_USER_ID:
                return basicKeyValue(field, FieldValueUserSingle::new, twin.getAssignerUser(), twin.getAssignerUserId());
            case CREATOR_USER_ID:
                return basicKeyValue(field, FieldValueUserSingle::new, twin.getCreatedByUser(), twin.getCreatedByUserId());
            case TWIN_STATUS_ID:
                return basicKeyValue(field, FieldValueStatus::new, twin.getTwinStatus(), twin.getTwinStatusId());
            case TWIN_CLASS_ID:
                return basicKeyValue(field, FieldValueTwinClassSingle::new, twin.getTwinClass(), twin.getTwinClassId());
            case HEAD_TWIN_ID:
                return basicKeyValue(field, FieldValueLinkSingle::new, twin.getHeadTwin(), twin.getHeadTwinId());
            // flavor has no entity relation on the twin: always an id-only reference
            case FLAVOR_DATA_LIST_OPTION_ID:
                return basicKeyValue(field, FieldValueSelectSingle::new, null, twin.getFlavorDataListOptionId());
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "Field " + field.getId() + " is not groupable");
        }
    }

    private static <E, V extends FieldValueItem<E>> FieldValue basicKeyValue(
            TwinClassFieldEntity field, Function<TwinClassFieldEntity, V> newValue, E entity, UUID id) {
        V empty = newValue.apply(field);
        if (entity != null) {
            return empty.setValue(entity);
        }
        return id == null ? empty : new FieldValueReferenceSingle(field, empty.getClass(), id);
    }
}
