package org.twins.core.dao.specifications.twin;


import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
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

    public static Specification<TwinEntity> checkFieldIsNull(String uuidField) {
        return (root, query, cb) -> cb.isNull(root.get(uuidField));
    }

    public static Specification<TwinEntity> checkUuid(String uuidField, UUID uuid) {
        return (root, query, cb) -> cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<TwinEntity> checkUuidIn(String uuidField, Collection<UUID> uuids) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinEntity> checkFieldLikeIn(String field, Collection<String> search, boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), "%" + s.toLowerCase() + "%");
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }


    public static Specification<TwinEntity> checkClass(Collection<UUID> twinClassUuids, ApiUser apiUser) throws ServiceException {
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
        return (root, query, cb) -> {
            if (!CollectionUtils.isEmpty(twinClassUuids)) {
                for (UUID twinClassId : twinClassUuids) {
                    Join<TwinClassEntity, TwinEntity> joinTwinClass = root.join(TwinEntity.Fields.twinClass, JoinType.INNER);
                    Predicate checkClassId = cb.equal(root.get(TwinEntity.Fields.twinClassId), twinClassId);
                    Predicate joinPredicateSystemLevel = cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), SYSTEM);
                    Predicate joinPredicateUserLevel = cb.or(
                            cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), USER),
                            cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                    );
                    Predicate joinPredicateBusinessLevel = cb.or(
                            cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), BUSINESS_ACCOUNT),
                            cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT),
                            cb.equal(joinTwinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                    );
                    Predicate rootPredicateUser = cb.equal(root.get(TwinEntity.Fields.ownerUserId), finalUserId);
                    Predicate rootPredicateBusiness = cb.equal(root.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId);
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
                                    cb.equal(root.get(TwinEntity.Fields.ownerUserId), finalUserId),
                                    cb.isNull(root.get(TwinEntity.Fields.ownerUserId)
                                    )
                            )
                    );
                } else predicate = cb.and(cb.isNull(root.get(TwinEntity.Fields.ownerUserId)));

                if (apiUser.isBusinessAccountSpecified()) {
                    predicate = cb.and(predicate, cb.or(
                                    cb.equal(root.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId),
                                    cb.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId)
                                    )
                            )
                    );
                } else cb.and(predicate, cb.isNull(root.get(TwinEntity.Fields.ownerBusinessAccountId)));
                return predicate;
            }
            return null;
        };
    }

    public static Specification<TwinEntity> checkTwinLinks(Map<UUID, Set<UUID>> twinLinksMap) {
        return (root, query, cb) -> {
            if (MapUtils.isNotEmpty(twinLinksMap)) {
                Root<TwinLinkEntity> twinLinkRoot = query.from(TwinLinkEntity.class);
                Join<TwinLinkEntity, TwinEntity> linkSrcTwin = twinLinkRoot.join(TwinLinkEntity.Fields.srcTwinId);
                Predicate prepicateLinkSrcTwin = cb.equal(linkSrcTwin, root.get(TwinEntity.Fields.id));
                List<Predicate> predicatesLinkListOr = new ArrayList<>();
                for (Map.Entry<UUID, Set<UUID>> linkDstTwinSet : twinLinksMap.entrySet()) {
                    if (CollectionUtils.isNotEmpty(linkDstTwinSet.getValue())) {
                        predicatesLinkListOr.add(
                                cb.and(
                                        cb.equal(twinLinkRoot.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()),
                                        twinLinkRoot.get(TwinLinkEntity.Fields.dstTwinId).in(linkDstTwinSet.getValue())
                                )
                        );
                    } else {
                        predicatesLinkListOr.add(cb.equal(twinLinkRoot.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()));
                    }
                    return cb.and(prepicateLinkSrcTwin, getPredicate(cb, predicatesLinkListOr, true));
                }
            }
            return cb.conjunction();
        };
    }

    public static Specification<TwinEntity> checkTwinLinksDummy(Map<UUID, Set<UUID>> twinLinksMap) {
        return (root, query, cb) -> cb.conjunction();
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
