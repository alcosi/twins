package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.DomainBusinessAccountUserSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserGroupField;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;
import org.twins.core.service.EntitySearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountUserSpecification.checkUserGroupIdIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainBusinessAccountUserSearchService extends EntitySearchService
        <DomainBusinessAccountUserSearch, DomainBusinessAccountUserEntity, DomainBusinessAccountUserSortField, DomainBusinessAccountUserGroupField> {
    private final DomainBusinessAccountUserRepository domainBusinessAccountUserRepository;

    @Override
    public JpaSpecificationExecutor<DomainBusinessAccountUserEntity> jpaSpecificationExecutor() {
        return domainBusinessAccountUserRepository;
    }

    @Override
    public DomainBusinessAccountUserSearch emptySearch() {
        return new DomainBusinessAccountUserSearch();
    }

    @Override
    protected DomainBusinessAccountUserEntity newEntity() {
        return new DomainBusinessAccountUserEntity();
    }

    public Specification<DomainBusinessAccountUserEntity> createFilterSpecification(DomainBusinessAccountUserSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, DomainBusinessAccountUserEntity.Fields.domainId),
                checkUuidIn(search.getUserIdList(), false, false, DomainBusinessAccountUserEntity.Fields.userId),
                checkUuidIn(search.getUserIdExcludeList(), true, false, DomainBusinessAccountUserEntity.Fields.userId),
                checkUuidIn(search.getBusinessAccountIdList(), false, false, DomainBusinessAccountUserEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountUserEntity.Fields.businessAccountId),
                checkUserGroupIdIn(search.getUserGroupIdList(), false),
                checkUserGroupIdIn(search.getUserGroupIdExcludeList(), true),
                checkFieldLocalDateTimeBetween(search.getLastActivityAtRange(), DomainBusinessAccountUserEntity.Fields.lastActivityAt),
                checkFieldLocalDateTimeBetween(search.getCreatedAtRange(), DomainBusinessAccountUserEntity.Fields.createdAt)
        );
    }

    @Override
    public Specification<DomainBusinessAccountUserEntity> createSortSpecification(DomainBusinessAccountUserSortField sortField, SortDirection sortDirection) {
        if (sortField == null)
            sortField = DomainBusinessAccountUserSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.createdAt);
            case lastActivityAt ->
                    toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.lastActivityAt);
            case userName ->
                    toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.user, UserEntity.Fields.name);
            case businessAccountName ->
                    toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);
        };
    }

    @Override
    public Specification<DomainBusinessAccountUserEntity> createCountSpecification(
            Set<DomainBusinessAccountUserGroupField> groupFields) {
        List<String> fields = new ArrayList<>(groupFields.size());

        for (var field : groupFields) {
            switch (field) {
                case userId ->
                        fields.add(DomainBusinessAccountUserEntity.Fields.userId);
                case businessAccountId ->
                        fields.add(DomainBusinessAccountUserEntity.Fields.businessAccountId);
            }
        }
        return toCountSpecification(fields);
    }

    @Override
    public void mapGroupedField(DomainBusinessAccountUserEntity entity, DomainBusinessAccountUserGroupField field, Object o) {
        switch (field) {
            case userId -> entity.setUserId((UUID) o);
            case businessAccountId -> entity.setBusinessAccountId((UUID) o);
        }
    }
}
