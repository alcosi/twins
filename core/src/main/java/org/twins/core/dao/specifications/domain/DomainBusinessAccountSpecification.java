package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountSortField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class DomainBusinessAccountSpecification extends CommonSpecification<DomainBusinessAccountEntity> {

    public static Specification<DomainBusinessAccountEntity> checkBusinessAccountFieldLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            Join<DomainBusinessAccountEntity, BusinessAccountEntity> baJoin = root.join(DomainBusinessAccountEntity.Fields.businessAccount, JoinType.INNER);
            if (search != null && !search.isEmpty()) {
                for (String name : search) {
                    Predicate predicate = cb.like(cb.lower(baJoin.get(field)), name.toLowerCase(), escapeChar);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DomainBusinessAccountEntity> checkBusinessAccountFieldNotLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<DomainBusinessAccountEntity, BusinessAccountEntity> baJoin = root.join(DomainBusinessAccountEntity.Fields.businessAccount, JoinType.INNER);
            if (search != null && !search.isEmpty()) {
                for (String name : search) {
                    Predicate predicate = cb.not(cb.like(cb.lower(baJoin.get(field)), name.toLowerCase(), escapeChar));
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DomainBusinessAccountEntity> checkUuid(final String fieldName, final UUID id) {
        return (root, query, cb) -> cb.equal(root.get(fieldName), id);
    }

    public static Specification<DomainBusinessAccountEntity> createSortSpecification(DomainBusinessAccountSearch search) {
        DomainBusinessAccountSortField sortField = search.getSortField();
        if (sortField == null)
            sortField = DomainBusinessAccountSortField.createdAt;
        boolean ascending = search.getSortDirection() != SortDirection.DESC;
        String[] fieldPath = switch (sortField) {
            case createdAt -> new String[]{DomainBusinessAccountEntity.Fields.createdAt};
            case businessAccountName -> new String[]{DomainBusinessAccountEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name};
            case permissionSchemaName -> new String[]{DomainBusinessAccountEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.name};
            case twinClassSchemaName -> new String[]{DomainBusinessAccountEntity.Fields.twinClassSchemaSpecOnly, TwinClassSchemaEntity.Fields.name};
            case twinflowSchemaName -> new String[]{DomainBusinessAccountEntity.Fields.twinflowSchemaSpecOnly, TwinflowSchemaEntity.Fields.name};
            case notificationSchemaName -> new String[]{DomainBusinessAccountEntity.Fields.notificationSchemaSpecOnly, NotificationSchemaEntity.Fields.nameI18n, };
            case tierName -> new String[]{DomainBusinessAccountEntity.Fields.tier, TierEntity.Fields.name};
            case attachmentsStorageUsedCount -> new String[]{DomainBusinessAccountEntity.Fields.attachmentsStorageUsedCount};
            case attachmentsStorageUsedSize -> new String[]{DomainBusinessAccountEntity.Fields.attachmentsStorageUsedSize};
        };
        return toSortSpecification(fieldPath, ascending);
    }

}
