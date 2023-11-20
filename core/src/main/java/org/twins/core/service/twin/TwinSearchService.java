package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    final EntityManager entityManager;
    final TwinService twinService;
    final TwinClassService twinClassService;
    @Lazy
    final AuthService authService;

    private Predicate createClassPredicate(UUID twinClassId, CriteriaBuilder criteriaBuilder, Root<TwinEntity> twin, ApiUser apiUser) throws ServiceException {
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

    private Predicate[] createWhere(BasicSearch basicSearch, CriteriaBuilder criteriaBuilder, CriteriaQuery criteriaQuery, Root<TwinEntity> root) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<Predicate> predicate = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinIdList()))
            predicate.add(root.get(TwinEntity.Fields.id).in(basicSearch.getTwinIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinClassIdList())) {
            List<Predicate> classPredicates = new ArrayList<>();
            for (UUID twinClassId : basicSearch.getTwinClassIdList())
                classPredicates.add(createClassPredicate(twinClassId, criteriaBuilder, root, apiUser));
            predicate.add(criteriaBuilder.or(classPredicates.toArray(Predicate[]::new)));
        } else { // no class filter, so we have to add force filtering by owner
            if (apiUser.isUserSpecified()) {
                predicate.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get(TwinEntity.Fields.ownerUserId), apiUser.getUser().getId()), //only user owned twins will be listed
                        criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerUserId))));
            } else
                predicate.add(criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerUserId)));
            if (apiUser.isBusinessAccountSpecified()) {
                predicate.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get(TwinEntity.Fields.ownerBusinessAccountId), apiUser.getBusinessAccount().getId()), //only businessAccount owned twins will be listed
                        criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId))));
            } else
                predicate.add(criteriaBuilder.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId)));
        }
        if (CollectionUtils.isNotEmpty(basicSearch.getAssignerUserIdList()))
            predicate.add(root.get(TwinEntity.Fields.assignerUserId).in(basicSearch.getAssignerUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getCreatedByUserIdList()))
            predicate.add(root.get(TwinEntity.Fields.createdByUserId).in(basicSearch.getCreatedByUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getStatusIdList()))
            predicate.add(root.get(TwinEntity.Fields.twinStatusId).in(basicSearch.getStatusIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getHeaderTwinIdList()))
            predicate.add(root.get(TwinEntity.Fields.headTwinId).in(basicSearch.getHeaderTwinIdList()));
        return predicate.stream().toArray(Predicate[]::new);
    }

    private CriteriaQuery<TwinEntity> getQuery(BasicSearch basicSearch) throws ServiceException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TwinEntity> criteriaQuery = criteriaBuilder.createQuery(TwinEntity.class);
        Root<TwinEntity> fromTwin = criteriaQuery.from(TwinEntity.class);
        return criteriaQuery
                .select(fromTwin)
                .where(createWhere(basicSearch, criteriaBuilder, criteriaQuery, fromTwin));
    }

    private CriteriaQuery<Long> getQueryForCount(BasicSearch basicSearch) throws ServiceException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<TwinEntity> fromTwin = criteriaQuery.from(TwinEntity.class);
        return criteriaQuery
                .select(criteriaBuilder.count(fromTwin))
                .where(createWhere(basicSearch, criteriaBuilder, criteriaQuery, fromTwin));
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        TypedQuery<TwinEntity> q = this.entityManager.createQuery(this.getQuery(basicSearch));
        List<TwinEntity> ret = q.getResultList();
        if (ret != null)
            return ret.stream().filter(t -> !twinService.isEntityReadDenied(t)).toList();
        return ret;
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        TypedQuery<Long> q = this.entityManager.createQuery(this.getQueryForCount(basicSearch));
        return q.getSingleResult();
    }
}
