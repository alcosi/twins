package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.dao.link.LinkValidatorRepository;
import org.twins.core.domain.search.LinkValidatorSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.LinkValidatorGroupField;
import org.twins.core.enums.sort.LinkValidatorSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkValidatorSearchService extends EntitySearchService
        <LinkValidatorSearch, LinkValidatorEntity, LinkValidatorSortField, LinkValidatorGroupField> {
    private final LinkValidatorRepository linkValidatorRepository;

    @Override
    public JpaSpecificationExecutor<LinkValidatorEntity> jpaSpecificationExecutor() {
        return linkValidatorRepository;
    }

    @Override
    public LinkValidatorSearch emptySearch() {
        return new LinkValidatorSearch();
    }

    @Override
    protected LinkValidatorEntity newEntity() {
        return new LinkValidatorEntity();
    }

    @Override
    protected Class<LinkValidatorEntity> entityClass() {
        return LinkValidatorEntity.class;
    }

    @Override
    protected LinkValidatorSortField defaultSortField() {
        return LinkValidatorSortField.order;
    }

    @Override
    public Specification<LinkValidatorEntity> createFilterSpecification(LinkValidatorSearch search, UUID domainId, Locale locale) throws ServiceException {
        // Domain isolation via link_validator -> link.domain_id (no domain_id on link_validator).
        return Specification.allOf(
                checkFieldUuid(domainId, LinkValidatorEntity.Fields.linkSpecOnly, LinkEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, LinkValidatorEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, LinkValidatorEntity.Fields.id),
                checkUuidIn(search.getLinkIdList(), false, false, LinkValidatorEntity.Fields.linkId),
                checkUuidIn(search.getLinkIdExcludeList(), true, false, LinkValidatorEntity.Fields.linkId),
                checkIntegerIn(search.getLinkerFeaturerIdList(), false, LinkValidatorEntity.Fields.linkerFeaturerId),
                checkIntegerIn(search.getLinkerFeaturerIdExcludeList(), true, LinkValidatorEntity.Fields.linkerFeaturerId)
        );
    }

    @Override
    public Specification<LinkValidatorEntity> createSortSpecification(LinkValidatorSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = defaultSortField();
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case order -> toSortSpecification(ascending, LinkValidatorEntity.Fields.order);
            case linkerFeaturerName -> toSortSpecification(ascending,
                    LinkValidatorEntity.Fields.linkerFeaturerSpecOnly, FeaturerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(LinkValidatorGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case linkId -> LinkValidatorEntity.Fields.linkId;
            case linkerFeaturerId -> LinkValidatorEntity.Fields.linkerFeaturerId;
        };
    }

    @Override
    public void mapGroupedField(LinkValidatorEntity entity, LinkValidatorGroupField field, Object o) {
        switch (field) {
            case linkId -> entity.setLinkId((UUID) o);
            case linkerFeaturerId -> entity.setLinkerFeaturerId((Integer) o);
        }
    }
}
