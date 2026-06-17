package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.LinkSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.enums.sort.LinkGroupField;
import org.twins.core.enums.sort.LinkSortField;
import org.twins.core.service.EntitySearchService;

import java.util.*;
import java.util.stream.Collectors;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.link.LinkSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class LinkSearchService extends EntitySearchService<LinkSearch, LinkEntity, LinkSortField, LinkGroupField> {
    private final LinkRepository linkRepository;

    /**
     * Backward-compatible entry point for the deprecated v1 search endpoint.
     * Use {@link #search(LinkSearch, SimplePagination, LinkSortField, SortDirection)} via v2 endpoint instead.
     */
    @Deprecated
    public PaginationResult<LinkEntity> findLinks(LinkSearch search, SimplePagination pagination) throws ServiceException {
        return search(search, pagination);
    }

    @Override
    public JpaSpecificationExecutor<LinkEntity> jpaSpecificationExecutor() {
        return linkRepository;
    }

    @Override
    public LinkSearch emptySearch() {
        return new LinkSearch();
    }

    @Override
    protected LinkEntity newEntity() {
        return new LinkEntity();
    }

    @Override
    protected Class<LinkEntity> entityClass() {
        return LinkEntity.class;
    }

    @Override
    public Specification<LinkEntity> createFilterSpecification(LinkSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkFieldUuid(domainId, LinkEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, LinkEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, LinkEntity.Fields.id),
                checkUuidIn(search.getSrcTwinClassIdList(), false, false, LinkEntity.Fields.srcTwinClassId),
                checkUuidIn(search.getSrcTwinClassIdExcludeList(), true, false, LinkEntity.Fields.srcTwinClassId),
                checkUuidIn(search.getDstTwinClassIdList(), false, false, LinkEntity.Fields.dstTwinClassId),
                checkUuidIn(search.getDstTwinClassIdExcludeList(), true, false, LinkEntity.Fields.dstTwinClassId),
                checkTernary(search.getSrcTwinClassInheritable(), LinkEntity.Fields.srcTwinClassInheritable),
                checkTernary(search.getDstTwinClassInheritable(), LinkEntity.Fields.dstTwinClassInheritable),
                checkSrcOrDstTwinClassIdIn(search.getSrcOrDstTwinClassIdList(), false),
                checkSrcOrDstTwinClassIdIn(search.getSrcOrDstTwinClassIdExcludeList(), true),
                joinAndSearchByI18NFieldDirect(LinkEntity.Fields.forwardNameI18nTranslationsSpecOnly, search.getForwardNameLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(LinkEntity.Fields.forwardNameI18nTranslationsSpecOnly, search.getForwardNameNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(LinkEntity.Fields.backwardNameI18nTranslationsSpecOnly, search.getBackwardNameLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(LinkEntity.Fields.backwardNameI18nTranslationsSpecOnly, search.getBackwardNameNotLikeList(), locale, true, true),
                checkFieldLikeIn(safeConvertTypeLink(search.getTypeLikeList()), false, true, LinkEntity.Fields.type),
                checkFieldLikeIn(safeConvertTypeLink(search.getTypeNotLikeList()), true, true, LinkEntity.Fields.type),
                checkFieldLikeIn(safeConvertStrengthLink(search.getStrengthLikeList()), false, true, LinkEntity.Fields.linkStrengthId),
                checkFieldLikeIn(safeConvertStrengthLink(search.getStrengthNotLikeList()), true, true, LinkEntity.Fields.linkStrengthId)
        );
    }

    @Override
    public Specification<LinkEntity> createSortSpecification(LinkSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = LinkSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt ->
                    toSortSpecification(ascending, LinkEntity.Fields.createdAt);
            case createdByUser ->
                    toSortSpecification(ascending, LinkEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
            case type ->
                    toSortSpecification(ascending, LinkEntity.Fields.type);
            case linkStrength ->
                    toSortSpecification(ascending, LinkEntity.Fields.linkStrengthId);
            case srcTwinClassName ->
                    I18nSpecification.toSortSpecificationDirect(ascending, locale, LinkEntity.Fields.srcTwinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case dstTwinClassName ->
                    I18nSpecification.toSortSpecificationDirect(ascending, locale, LinkEntity.Fields.dstTwinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case forwardName ->
                    I18nSpecification.toSortSpecificationDirect(ascending, locale, LinkEntity.Fields.forwardNameI18nTranslationsSpecOnly);
            case backwardName ->
                    I18nSpecification.toSortSpecificationDirect(ascending, locale, LinkEntity.Fields.backwardNameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(LinkGroupField groupField) {
        return switch (groupField) {
            case srcTwinClassId -> LinkEntity.Fields.srcTwinClassId;
            case dstTwinClassId -> LinkEntity.Fields.dstTwinClassId;
            case type -> LinkEntity.Fields.type;
            case linkStrength -> LinkEntity.Fields.linkStrengthId;
            case createdByUserId -> LinkEntity.Fields.createdByUserId;
            case srcTwinClassInheritable -> LinkEntity.Fields.srcTwinClassInheritable;
            case dstTwinClassInheritable -> LinkEntity.Fields.dstTwinClassInheritable;
        };
    }

    @Override
    public void mapGroupedField(LinkEntity entity, LinkGroupField field, Object o) {
        switch (field) {
            case srcTwinClassId -> entity.setSrcTwinClassId((UUID) o);
            case dstTwinClassId -> entity.setDstTwinClassId((UUID) o);
            case type -> entity.setType((LinkType) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
            case linkStrength -> entity.setLinkStrengthId((LinkStrength) o);
            case srcTwinClassInheritable -> entity.setSrcTwinClassInheritable((Boolean) o);
            case dstTwinClassInheritable -> entity.setDstTwinClassInheritable((Boolean) o);
        }
    }

    private Set<String> safeConvertTypeLink(Collection<LinkType> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }

    private Set<String> safeConvertStrengthLink(Collection<LinkStrength> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
