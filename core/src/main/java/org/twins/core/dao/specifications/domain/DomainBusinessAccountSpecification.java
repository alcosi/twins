package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountSortField;

import java.util.*;

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

    public static Specification<DomainBusinessAccountEntity> createSortSpecification(DomainBusinessAccountSearch search, Locale locale) {
        DomainBusinessAccountSortField sortField = search.getSortField();
        if (sortField == null)
            sortField = DomainBusinessAccountSortField.createdAt;
        boolean ascending = search.getSortDirection() != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.createdAt);
            case businessAccountName -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);
            case permissionSchemaName -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.name);
            case twinClassSchemaName -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.twinClassSchemaSpecOnly, TwinClassSchemaEntity.Fields.name);
            case twinflowSchemaName -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.twinflowSchemaSpecOnly, TwinflowSchemaEntity.Fields.name);
            case notificationSchemaName -> I18nSpecification.toSortSpecification(
                    ascending, locale,
                    DomainBusinessAccountEntity.Fields.notificationSchemaSpecOnly, NotificationSchemaEntity.Fields.nameI18n);
            case tierName -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.tier, TierEntity.Fields.name);
            case attachmentsStorageUsedCount -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.attachmentsStorageUsedCount);
            case attachmentsStorageUsedSize -> toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.attachmentsStorageUsedSize);
        };
    }

}
