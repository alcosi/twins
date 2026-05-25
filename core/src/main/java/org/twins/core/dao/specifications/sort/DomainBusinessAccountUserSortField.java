package org.twins.core.dao.specifications.sort;

import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.specifications.SortField;
import org.twins.core.dao.user.UserEntity;

public enum DomainBusinessAccountUserSortField implements SortField<DomainBusinessAccountUserEntity> {
    createdAt(DomainBusinessAccountUserEntity.Fields.createdAt),
    lastActivityAt(DomainBusinessAccountUserEntity.Fields.lastActivityAt),
    userName(DomainBusinessAccountUserEntity.Fields.user, UserEntity.Fields.name),
    businessAccountName(DomainBusinessAccountUserEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);

    private final String[] fieldPath;

    DomainBusinessAccountUserSortField(String... fieldPath) {
        this.fieldPath = fieldPath;
    }

    @Override
    public String[] fieldPath() {
        return fieldPath;
    }

}
