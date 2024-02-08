package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;

import java.util.*;

import static org.twins.core.dao.twinclass.TwinClassEntity.OwnerType.*;


@Slf4j
public class TwinSpecification {

    private TwinSpecification() {
    }

    public static Specification<TwinEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(uuidField).in(uuids).not() : root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), s.toLowerCase());
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinEntity> checkClass(final Collection<UUID> twinClassUuids, final ApiUser apiUser) {
        UUID userId = null;
        UUID businessAccountId = null;
        try {
            userId = apiUser.getUser().getId();
            businessAccountId = apiUser.getBusinessAccount().getId();
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
        UUID finalUserId = userId;
        UUID finalBusinessAccountId = businessAccountId;
        return (twin, query, cb) -> {
            if (!CollectionUtils.isEmpty(twinClassUuids)) {
                for (UUID twinClassId : twinClassUuids) {
                    Join<TwinClassEntity, TwinEntity> twinClass = twin.join(TwinEntity.Fields.twinClass);
                    Predicate checkClassId = cb.equal(twin.get(TwinEntity.Fields.twinClassId), twinClassId);
                    Predicate joinPredicateSystemLevel = cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), SYSTEM);
                    Predicate joinPredicateUserLevel = cb.or(
                            cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), USER),
                            cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                    );
                    Predicate joinPredicateBusinessLevel = cb.or(
                            cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), BUSINESS_ACCOUNT),
                            cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT),
                            cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                    );
                    Predicate rootPredicateUser = cb.equal(twin.get(TwinEntity.Fields.ownerUserId), finalUserId);
                    Predicate rootPredicateBusiness = cb.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId);
                    return cb.and(
                            checkClassId,
                            cb.or(
                                    cb.and(joinPredicateUserLevel, rootPredicateUser),
                                    cb.and(joinPredicateBusinessLevel, rootPredicateBusiness)
                                    //todo system level:  add Subquery to detect valid user and business account twins
                            )
                    );

                }
            } else { // no class filter, so we have to add force filtering by owner
                Predicate predicate;
                if (apiUser.isUserSpecified()) {
                    predicate = cb.and(cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerUserId), finalUserId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)
                                    )
                            )
                    );
                } else predicate = cb.and(cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)));

                if (apiUser.isBusinessAccountSpecified()) {
                    predicate = cb.and(predicate, cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)
                                    )
                            )
                    );
                } else cb.and(predicate, cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)));
                return predicate;
            }
            return null;
        };
    }


    public static Specification<TwinEntity> checkTwinLinks(Map<UUID, Set<UUID>> twinLinksMap, Map<UUID, Set<UUID>> noTwinLinksMap) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (MapUtils.isNotEmpty(twinLinksMap)) {
                Join<TwinEntity, TwinLinkEntity> linkSrcTwin = root.join(TwinEntity.Fields.linksBySrcTwinId);
//                Join<TwinEntity, TwinLinkEntity> linkDstTwin = root.join(TwinEntity.Fields.linksByDstTwinId);
                for (Map.Entry<UUID, Set<UUID>> entry : twinLinksMap.entrySet()) {
                    Predicate linkCondition = cb.equal(linkSrcTwin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate dstTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    predicates.add(cb.and(linkCondition, dstTwinCondition));
                }
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates, boolean or) {
        int size = predicates.size();
        if (size == 0) {
            return cb.conjunction();
        } else {
            Predicate[] stockArr = new Predicate[size];
            stockArr = predicates.toArray(stockArr);
            return or ? cb.or(stockArr) : cb.and(stockArr);
        }
    }
}
