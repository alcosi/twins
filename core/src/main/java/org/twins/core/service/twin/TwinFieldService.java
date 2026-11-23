package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Twin field VALUE access. Today it is the single read point behind twin grouping; the rest of the
 * field-value machinery (parseFieldValue / materializeFieldValues / createFieldValue, still on
 * TwinService) migrates here over time.
 */
@Service
@RequiredArgsConstructor
public class TwinFieldService {
    // createFieldValue until the value plumbing migrates here
    private final TwinService twinService;

    /**
     * Base (system) fields getFieldValue shapes into a single group key value; keep in sync with the
     * getFieldValue switch. Tags, markers and aliases are system fields too, but have no scalar
     * column to group by.
     */
    public static final Set<TwinEntity.BasicField> GROUPABLE_BASIC_FIELDS = Set.of(
            TwinEntity.BasicField.OWNER_USER_ID,
            TwinEntity.BasicField.ASSIGNEE_USER_ID,
            TwinEntity.BasicField.CREATOR_USER_ID,
            TwinEntity.BasicField.TWIN_STATUS_ID,
            TwinEntity.BasicField.TWIN_CLASS_ID,
            TwinEntity.BasicField.HEAD_TWIN_ID,
            TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID);

    /**
     * The current value of one field on one twin — the single read point for twin grouping
     * (TwinGroupLevel.extractKeys). A dynamic field reads twin.fieldValuesKit (filled by
     * TwinService.loadFieldsValues); an absent value comes back as an undefined value of the field's
     * type, never null, so the group key type survives even without a value. A base field lives in
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
        if (!GROUPABLE_BASIC_FIELDS.contains(basicField)) {
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
