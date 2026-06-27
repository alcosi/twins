package org.twins.core.service;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.consts.SystemIds;

import java.util.Set;
import java.util.UUID;

/**
 * Static lookup helpers over {@link SystemIds} constants — predicates and value extractors
 * used across the runtime to branch on system TwinClasses / TwinClassFields.
 *
 * <p>Pure leaf in the dependency graph — depends only on {@link SystemIds} (constants) and
 * {@link TwinEntity.BasicField} (for value extraction). No Spring wiring, no DB access.
 *
 * <p>When a new system TwinClass or TwinClassField is added to {@code SystemBootstrapData},
 * update {@link SystemIds.TwinClassField.Base#ALL_FIELDS_SET} so {@link #isSystemField(UUID)}
 * recognises it.
 */
public final class SystemIdLookup {
    private SystemIdLookup() {}

    public static boolean isTwinClassForUser(UUID twinClassId) {
        return SystemIds.TwinClass.USER.equals(twinClassId);
    }

    public static boolean isTwinClassForBusinessAccount(UUID twinClassId) {
        return SystemIds.TwinClass.BUSINESS_ACCOUNT.equals(twinClassId);
    }

    public static boolean isSystemClass(UUID twinClassId) {
        return isTwinClassForUser(twinClassId) || isTwinClassForBusinessAccount(twinClassId);
    }

    public static Set<UUID> getSystemFieldsIds() {
        return SystemIds.TwinClassField.Base.ALL_FIELDS_SET;
    }

    public static boolean isSystemField(UUID fieldId) {
        return SystemIds.TwinClassField.Base.ALL_FIELDS_SET.contains(fieldId);
    }

    public static Object getSystemFieldValue(TwinEntity twinEntity, UUID systemFieldId) throws ServiceException {
        if (systemFieldId == null || twinEntity == null) {
            return null;
        }
        var basicField = TwinEntity.BasicField.convertOrNull(systemFieldId);
        if (basicField != null) {
            return basicField.getValue(twinEntity);
        }
        return null;
    }
}
