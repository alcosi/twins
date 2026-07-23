package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.TwinLinkSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinLinkGroupField;
import org.twins.core.enums.sort.TwinLinkSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLocalDateTimeBetween;
import static org.twins.core.dao.specifications.link.TwinLinkSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinLinkSearchService extends EntitySearchService<TwinLinkSearch, TwinLinkEntity, TwinLinkSortField, TwinLinkGroupField> {
    private final TwinLinkRepository twinLinkRepository;

    @Override
    public JpaSpecificationExecutor<TwinLinkEntity> jpaSpecificationExecutor() {
        return twinLinkRepository;
    }

    @Override
    public TwinLinkSearch emptySearch() {
        return new TwinLinkSearch();
    }

    @Override
    protected TwinLinkEntity newEntity() {
        return new TwinLinkEntity();
    }

    @Override
    protected Class<TwinLinkEntity> entityClass() {
        return TwinLinkEntity.class;
    }

    @Override
    public Specification<TwinLinkEntity> createFilterSpecification(TwinLinkSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinLinkEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinLinkEntity.Fields.id),
                checkUuidIn(search.getSrcTwinIdList(), false, false, TwinLinkEntity.Fields.srcTwinId),
                checkUuidIn(search.getSrcTwinIdExcludeList(), true, false, TwinLinkEntity.Fields.srcTwinId),
                checkUuidIn(search.getDstTwinIdList(), false, false, TwinLinkEntity.Fields.dstTwinId),
                checkUuidIn(search.getDstTwinIdExcludeList(), true, false, TwinLinkEntity.Fields.dstTwinId),
                checkSrcOrDstTwinIdIn(search.getSrcOrDstTwinIdList(), false),
                checkSrcOrDstTwinIdIn(search.getSrcOrDstTwinIdExcludeList(), true),
                checkUuidIn(search.getLinkIdList(), false, false, TwinLinkEntity.Fields.linkId),
                checkUuidIn(search.getLinkIdExcludeList(), true, false, TwinLinkEntity.Fields.linkId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinLinkEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, false, TwinLinkEntity.Fields.createdByUserId),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), TwinLinkEntity.Fields.createdAt)
        );
    }

    @Override
    public Specification<TwinLinkEntity> createSortSpecification(TwinLinkSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = TwinLinkSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt ->
                    toSortSpecification(ascending, TwinLinkEntity.Fields.createdAt);
            case createdByUserName ->
                    toSortSpecification(ascending, TwinLinkEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
            case srcTwinName ->
                    toSortSpecification(ascending, TwinLinkEntity.Fields.srcTwinSpecOnly, TwinEntity.Fields.name);
            case dstTwinName ->
                    toSortSpecification(ascending, TwinLinkEntity.Fields.dstTwinSpecOnly, TwinEntity.Fields.name);
            case linkName ->
                    I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinLinkEntity.Fields.linkSpecOnly, LinkEntity.Fields.forwardNameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(TwinLinkGroupField groupField) {
        return switch (groupField) {
            case srcTwinId -> TwinLinkEntity.Fields.srcTwinId;
            case dstTwinId -> TwinLinkEntity.Fields.dstTwinId;
            case linkId -> TwinLinkEntity.Fields.linkId;
            case createdByUserId -> TwinLinkEntity.Fields.createdByUserId;
        };
    }

    @Override
    public void mapGroupedField(TwinLinkEntity entity, TwinLinkGroupField field, Object o) {
        switch (field) {
            case srcTwinId -> entity.setSrcTwinId((UUID) o);
            case dstTwinId -> entity.setDstTwinId((UUID) o);
            case linkId -> entity.setLinkId((UUID) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
        }
    }
}
