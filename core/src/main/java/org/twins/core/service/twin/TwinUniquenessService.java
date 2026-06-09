package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.cambium.featurer.FeaturerService;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassUniquenessConfigService;
import org.twins.core.service.twinclass.TwinClassUniquenessService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinUniquenessService {

    private final TwinClassUniquenessConfigService configService;
    private final TwinClassUniquenessCompositeRepository compositeRepository;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassUniquenessService twinClassUniquenessService;
    private final FeaturerService featurerService;

    public void checkOnCreate(TwinCreate twinCreate) throws ServiceException {
        if (twinCreate.getFields() == null || twinCreate.getFields().isEmpty()) {
            return;
        }

        TwinEntity twinEntity = twinCreate.getTwinEntity();
        if (twinEntity == null || twinEntity.getTwinClass() == null) {
            return;
        }

        check(twinEntity, twinCreate.getFields(), null);
    }

    public void checkOnUpdate(TwinEntity twin, Map<UUID, FieldValue> fields) throws ServiceException {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        check(twin, fields, twin.getId());
    }

    public void check(TwinEntity twin, Map<UUID, FieldValue> fieldValues, UUID excludeTwinId) throws ServiceException {
        TwinClassEntity twinClass = twin.getTwinClass();
        if (twinClass == null) {
            twinClass = twinClassService.findEntitySafe(twin.getTwinClassId());
        }

        twinClassUniquenessService.loadUniqueness(twinClass);

        Kit<TwinClassUniquenessEntity, UUID> uniquenessKit = twinClass.getUniquenessKit();
        if (uniquenessKit == null || uniquenessKit.isEmpty()) {
            return;
        }

        for (TwinClassUniquenessEntity uniqueness : uniquenessKit.getCollection()) {
            if (!isApplicableToClass(twinClass, uniqueness)) {
                continue;
            }

            configService.loadUniquenessFields(uniqueness);
            if (uniqueness.getFieldKit() == null || uniqueness.getFieldKit().isEmpty()) {
                continue;
            }

            checkUniqueness(twin, uniqueness, fieldValues, excludeTwinId);
        }
    }

    private boolean isApplicableToClass(TwinClassEntity twinClass, TwinClassUniquenessEntity uniqueness) {
        if (uniqueness.getInheritable() != null && uniqueness.getInheritable()) {
            return true;
        }
        return uniqueness.getTwinClassId().equals(twinClass.getId());
    }

    private void checkUniqueness(TwinEntity twin, TwinClassUniquenessEntity uniqueness,
                                Map<UUID, FieldValue> fieldValues, UUID excludeTwinId) throws ServiceException {
        List<UUID> fieldIds = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<UUID> optionIds = new ArrayList<>();
        List<UUID> userIds = new ArrayList<>();
        Set<Integer> fieldTyperIds = new HashSet<>();

        for (TwinClassUniquenessFieldEntity fieldEntity : uniqueness.getFieldKit().getCollection()) {
            TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(fieldEntity.getTwinClassFieldId());
            FieldValue fieldValue = fieldValues.get(field.getId());

            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }

            fieldIds.add(field.getId());
            fieldTyperIds.add(field.getFieldTyperFeaturerId());

            Object normalizedValue = normalizeFieldValue(field, fieldValue);
            if (normalizedValue == null) {
                continue;
            }

            if (normalizedValue instanceof String) {
                values.add((String) normalizedValue);
            } else if (normalizedValue instanceof UUID) {
                UUID uuidValue = (UUID) normalizedValue;
                optionIds.add(uuidValue);
                userIds.add(uuidValue);
            } else if (normalizedValue instanceof BigDecimal) {
                values.add(normalizedValue.toString());
            } else if (normalizedValue instanceof Timestamp) {
                values.add(normalizedValue.toString());
            }
        }

        if (fieldIds.isEmpty()) {
            return;
        }

        Collection<UUID> scopeClassIds = getScopeClassIds(twin.getTwinClass(), uniqueness);

        OwnerType ownerType = twin.getTwinClass().getOwnerType();
        UUID ownerUserId = null;
        UUID ownerBusinessAccountId = null;

        switch (ownerType) {
            case USER, DOMAIN_USER -> ownerUserId = twin.getOwnerUserId();
            case BUSINESS_ACCOUNT, DOMAIN_BUSINESS_ACCOUNT -> ownerBusinessAccountId = twin.getOwnerBusinessAccountId();
        }

        UUID finalExcludeTwinId = excludeTwinId != null ? excludeTwinId : UUID.fromString("00000000-0000-0000-0000-000000000000");

        boolean exists = false;
        if (fieldTyperIds.size() == 1) {
            int fieldTyperId = fieldTyperIds.iterator().next();
            exists = checkSingleFieldType(fieldTyperId, finalExcludeTwinId, scopeClassIds,
                    ownerUserId, ownerBusinessAccountId, fieldIds, values, optionIds, userIds);
        } else {
            exists = checkMultipleFieldTypes(finalExcludeTwinId, scopeClassIds,
                    ownerUserId, ownerBusinessAccountId, fieldIds, values, optionIds, userIds);
        }

        if (exists) {
            throw new ServiceException(ErrorCodeTwins.TWIN_COMPOSITE_NOT_UNIQUE,
                    "Composite fields values are not unique. Uniqueness: " + uniqueness.getKey() +
                    ", TwinClass: " + twin.getTwinClass().getKey());
        }
    }

    private Collection<UUID> getScopeClassIds(TwinClassEntity twinClass, TwinClassUniquenessEntity uniqueness) {
        if (uniqueness.getInheritable() != null && uniqueness.getInheritable()) {
            return twinClass.getExtendedClassIdSet();
        }
        return Collections.singleton(twinClass.getId());
    }

    private boolean checkSingleFieldType(int fieldTyperId, UUID excludeTwinId, Collection<UUID> scopeClassIds,
                                         UUID ownerUserId, UUID ownerBusinessAccountId,
                                         List<UUID> fieldIds, List<String> values,
                                         List<UUID> optionIds, List<UUID> userIds) {
        if (fieldTyperId >= 1300 && fieldTyperId < 1400) {
            return compositeRepository.existsBySimpleFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, values, fieldIds.size()
            );
        } else if (fieldTyperId >= 1400 && fieldTyperId < 1500) {
            return compositeRepository.existsByDecimalFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, values, fieldIds.size()
            );
        } else if (fieldTyperId >= 1500 && fieldTyperId < 1600) {
            return compositeRepository.existsByTimestampFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, values, fieldIds.size()
            );
        } else if (fieldTyperId >= 1700 && fieldTyperId < 1800) {
            return compositeRepository.existsByDatalistFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, optionIds, fieldIds.size()
            );
        } else if (fieldTyperId >= 1600 && fieldTyperId < 1700) {
            return compositeRepository.existsByUserFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, userIds, fieldIds.size()
            );
        }
        return false;
    }

    private boolean checkMultipleFieldTypes(UUID excludeTwinId, Collection<UUID> scopeClassIds,
                                             UUID ownerUserId, UUID ownerBusinessAccountId,
                                             List<UUID> fieldIds, List<String> values,
                                             List<UUID> optionIds, List<UUID> userIds) {
        if (!values.isEmpty()) {
            if (compositeRepository.existsBySimpleFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, values, fieldIds.size())) {
                return true;
            }
        }
        if (!optionIds.isEmpty()) {
            if (compositeRepository.existsByDatalistFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, optionIds, fieldIds.size())) {
                return true;
            }
        }
        if (!userIds.isEmpty()) {
            if (compositeRepository.existsByUserFieldsComposite(
                    excludeTwinId, scopeClassIds, ownerUserId, ownerBusinessAccountId,
                    fieldIds, userIds, fieldIds.size())) {
                return true;
            }
        }
        return false;
    }

    private Object normalizeFieldValue(TwinClassFieldEntity field, FieldValue fieldValue) {
        try {
            FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);

            if (fieldValue instanceof FieldValueText) {
                FieldValueText textValue = (FieldValueText) fieldValue;
                String value = textValue.getValue();
                if (value != null) {
                    return value.trim();
                }
            } else if (fieldValue instanceof FieldValueSimple) {
                FieldValueSimple simpleValue = (FieldValueSimple) fieldValue;
                return simpleValue.getValue();
            } else if (fieldValue instanceof FieldValueDate) {
                FieldValueDate dateValue = (FieldValueDate) fieldValue;
                LocalDateTime localDateTime = dateValue.getDate();
                if (localDateTime != null) {
                    return Timestamp.valueOf(localDateTime);
                }
            } else if (fieldValue instanceof FieldValueSelect) {
                FieldValueSelect selectValue = (FieldValueSelect) fieldValue;
                if (!selectValue.getItems().isEmpty()) {
                    return selectValue.getItems().iterator().next().getId();
                }
            } else if (fieldValue instanceof FieldValueUser) {
                FieldValueUser userValue = (FieldValueUser) fieldValue;
                if (!userValue.getItems().isEmpty()) {
                    return userValue.getItems().iterator().next().getId();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to normalize field value for uniqueness check", e);
        }
        return null;
    }
}
