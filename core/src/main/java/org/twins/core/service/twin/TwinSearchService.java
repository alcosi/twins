package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.JPACriteriaQueryStub;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    final EntityManager entityManager;
    final TwinService twinService;
    final TwinClassService twinClassService;
    @Lazy
    final AuthService authService;

    private Predicate createClassPredicate(UUID twinClassId, CriteriaBuilder criteriaBuilder, Path<TwinEntity> twin, ApiUser apiUser) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);
        List<Predicate> predicate = new ArrayList<>();
        predicate.add(criteriaBuilder.equal(twin.get(TwinEntity.Fields.twinClassId), twinClassId));
        if (twinClassEntity.getOwnerType().isUserLevel())
            predicate.add(criteriaBuilder.equal(twin.get(TwinEntity.Fields.ownerUserId), apiUser.getUser().getId())); //only user owned twins will be listed
        if (twinClassEntity.getOwnerType().isBusinessAccountLevel())
            predicate.add(criteriaBuilder.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), apiUser.getBusinessAccount().getId())); //only businessAccount owned twins will be listed
        if (twinClassEntity.getOwnerType().isSystemLevel()) {
            //todo add Subquery to detect valid user and business account twins
        }
        return criteriaBuilder.and(predicate.toArray(Predicate[]::new));
    }

    private List<Predicate> createTwinEntityPredicates(BasicSearch basicSearch, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Path<TwinEntity> root) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<Predicate> predicateList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinIdList()))
            predicateList.add(root.get(TwinEntity.Fields.id).in(basicSearch.getTwinIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinIdExcludeList()))
            predicateList.add(root.get(TwinEntity.Fields.id).in(basicSearch.getTwinIdExcludeList()).not());
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinNameLikeList())) {
            List<Predicate> namePredicates = new ArrayList<>();
            for (String twinNameLike : basicSearch.getTwinNameLikeList())
                namePredicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get(TwinEntity.Fields.name)), twinNameLike.toUpperCase()));
            predicateList.add(criteriaBuilder.or(namePredicates.toArray(Predicate[]::new)));
        }
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinClassIdList())) {
            List<Predicate> classPredicates = new ArrayList<>();
            for (UUID twinClassId : basicSearch.getTwinClassIdList())
                classPredicates.add(createClassPredicate(twinClassId, criteriaBuilder, root, apiUser));
            predicateList.add(criteriaBuilder.or(classPredicates.toArray(Predicate[]::new)));
        } else { // no class filter, so we have to add force filtering by owner
            if (apiUser.isUserSpecified()) {
                predicateList.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get(TwinEntity.Fields.ownerUserId), apiUser.getUser().getId()), //only user owned twins will be listed
                        criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerUserId))));
            } else
                predicateList.add(criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerUserId)));
            if (apiUser.isBusinessAccountSpecified()) {
                predicateList.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get(TwinEntity.Fields.ownerBusinessAccountId), apiUser.getBusinessAccount().getId()), //only businessAccount owned twins will be listed
                        criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId))));
            } else
                predicateList.add(criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId)));
        }
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        if (CollectionUtils.isNotEmpty(basicSearch.getAssignerUserIdList()))
            predicateList.add(root.get(TwinEntity.Fields.assignerUserId).in(basicSearch.getAssignerUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getCreatedByUserIdList()))
            predicateList.add(root.get(TwinEntity.Fields.createdByUserId).in(basicSearch.getCreatedByUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getStatusIdList()))
            predicateList.add(root.get(TwinEntity.Fields.twinStatusId).in(basicSearch.getStatusIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getHeaderTwinIdList()))
            predicateList.add(root.get(TwinEntity.Fields.headTwinId).in(basicSearch.getHeaderTwinIdList()));
        //        if (MapUtils.isNotEmpty(basicSearch.getTwinLinksMap())) {
//            Root<TwinLinkEntity> fromTwinLinks = criteriaQuery.from(TwinLinkEntity.class);
//            for (Map.Entry<UUID, Set<UUID>> linkDstTwinSet : basicSearch.getTwinLinksMap().entrySet()) {
//                predicate.add(criteriaBuilder.equal(fromTwinLinks.get(TwinLinkEntity.Fields.srcTwinId), root.get(TwinEntity.Fields.id))); //join unassociated entities
//                predicate.add(criteriaBuilder.equal(fromTwinLinks.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()));
//                predicate.add(fromTwinLinks.get(TwinLinkEntity.Fields.dstTwinId).in(linkDstTwinSet.getValue()));
//            }
//        }
        return predicateList;
    }

    private List<Predicate> createTwinLinkEntityPredicates(BasicSearch basicSearch, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Path<TwinLinkEntity> linkPath) throws ServiceException {
        List<Predicate> predicate = new ArrayList<>();
        if (MapUtils.isNotEmpty(basicSearch.getTwinLinksMap())) {
            List<Predicate> orPredicate = new ArrayList<>();
            for (Map.Entry<UUID, Set<UUID>> linkDstTwinSet : basicSearch.getTwinLinksMap().entrySet()) {
                orPredicate.add(criteriaBuilder.and(
                                        criteriaBuilder.equal(linkPath.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()),
                                        linkPath.get(TwinLinkEntity.Fields.dstTwinId).in(linkDstTwinSet.getValue())
                                ));
            }
            predicate.add(criteriaBuilder.or(orPredicate.toArray(Predicate[]::new)));
        }
        return predicate;
    }

    private CriteriaQuery<TwinEntity> getQuery(BasicSearch basicSearch) throws ServiceException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TwinEntity> criteriaQuery = criteriaBuilder.createQuery(TwinEntity.class);
        JPACriteriaQueryStub queryStub = createQueryStub(basicSearch, criteriaBuilder, criteriaQuery);
        return criteriaQuery
                .select((Selection<? extends TwinEntity>) queryStub.getSelect())
                .where(queryStub.getWhere())
                .orderBy(criteriaBuilder.desc(queryStub.getSelect().get(TwinEntity.Fields.createdAt)));
    }

    private CriteriaQuery<Long> getQueryForCount(BasicSearch basicSearch) throws ServiceException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        JPACriteriaQueryStub queryStub = createQueryStub(basicSearch, criteriaBuilder, criteriaQuery);
        return criteriaQuery
                .select(criteriaBuilder.count(queryStub.getSelect()))
                .where(queryStub.getWhere());
    }

    protected JPACriteriaQueryStub createQueryStub(BasicSearch basicSearch, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery) throws ServiceException {
        Path<TwinEntity> select;
        List<Predicate> wherePredicateList;
        if (MapUtils.isNotEmpty(basicSearch.getTwinLinksMap())) {
            Root<TwinLinkEntity> fromTwinLink = criteriaQuery.from(TwinLinkEntity.class);
            select = fromTwinLink.get(TwinLinkEntity.Fields.srcTwin);
            wherePredicateList = createTwinLinkEntityPredicates(basicSearch, criteriaBuilder, criteriaQuery, fromTwinLink);
            Join<TwinLinkEntity, TwinEntity> linkSrcTwin = fromTwinLink.join(TwinLinkEntity.Fields.srcTwin);
            wherePredicateList.addAll(createTwinEntityPredicates(basicSearch, criteriaBuilder, criteriaQuery, linkSrcTwin));
        } else {
            select = criteriaQuery.from(TwinEntity.class);
            wherePredicateList= createTwinEntityPredicates(basicSearch, criteriaBuilder, criteriaQuery, select);
        }
        return new JPACriteriaQueryStub()
                .setSelect(select)
                .setWhere(wherePredicateList.toArray(Predicate[]::new));
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        TypedQuery<TwinEntity> q = entityManager.createQuery(getQuery(basicSearch));
        List<TwinEntity> ret = q.getResultList();
        if (ret != null)
            return ret.stream().filter(t -> !twinService.isEntityReadDenied(t)).toList();
        return ret;
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        TypedQuery<Long> q = entityManager.createQuery(getQueryForCount(basicSearch));
        return q.getSingleResult();
    }
}
