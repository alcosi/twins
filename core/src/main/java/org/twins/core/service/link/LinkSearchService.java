package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.LinkSearch;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.link.LinkSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class LinkSearchService {
    private final LinkRepository linkRepository;
    private final AuthService authService;


    public PaginationResult<LinkEntity> findLinks(LinkSearch search, SimplePagination pagination) throws ServiceException {
        Specification<LinkEntity> spec = createLinkSearchSpecification(search);
        Page<LinkEntity> ret = linkRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<LinkEntity> createLinkSearchSpecification(LinkSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), LinkEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, LinkEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, LinkEntity.Fields.id),
                checkUuidIn(search.getSrcTwinClassIdList(), false, false, LinkEntity.Fields.srcTwinClassId),
                checkUuidIn(search.getSrcTwinClassIdExcludeList(), true, false, LinkEntity.Fields.srcTwinClassId),
                checkUuidIn(search.getDstTwinClassIdList(), false, false, LinkEntity.Fields.dstTwinClassId),
                checkUuidIn(search.getDstTwinClassIdExcludeList(), true, false, LinkEntity.Fields.dstTwinClassId),
                checkSrcOrDstTwinClassIdIn(search.getSrcOrDstTwinClassIdList(), false),
                checkSrcOrDstTwinClassIdIn(search.getSrcOrDstTwinClassIdExcludeList(), true),
                joinAndSearchByI18NField(LinkEntity.Fields.forwardNameI18n, search.getForwardNameLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(LinkEntity.Fields.forwardNameI18n, search.getForwardNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(LinkEntity.Fields.backwardNameI18n, search.getBackwardNameLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(LinkEntity.Fields.backwardNameI18n, search.getBackwardNameNotLikeList(), apiUser.getLocale(), true, true),
                checkFieldLikeIn(safeConvertTypeLink(search.getTypeLikeList()), false, true, LinkEntity.Fields.type),
                checkFieldLikeIn(safeConvertTypeLink(search.getTypeNotLikeList()), true, true, LinkEntity.Fields.type),
                checkFieldLikeIn(safeConvertStrengthLink(search.getStrengthLikeList()), false, true, LinkEntity.Fields.linkStrengthId),
                checkFieldLikeIn(safeConvertStrengthLink(search.getStrengthNotLikeList()), true, true, LinkEntity.Fields.linkStrengthId)
        );
    }

    private Set<String> safeConvertTypeLink(Collection<LinkType> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }

    private Set<String> safeConvertStrengthLink(Collection<LinkStrength> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
