package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;

import java.util.*;

import static org.twins.core.dao.twinclass.TwinClassEntity.OwnerType.*;


@Slf4j
public class TwinSpecification {

    private TwinSpecification() {
    }

    public static Specification<TwinEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean or) {
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

    public static Specification<TwinEntity> checkClass(final Collection<UUID> twinClassUuids,final ApiUser apiUser) {
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
                    Join<TwinClassEntity, TwinEntity> twinClass = twin.join(TwinEntity.Fields.twinClass, JoinType.INNER);
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
                            cb.or(//todo - hope its not "and"...write some cases on paper
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



    public static Specification<TwinEntity> checkJoinLinksSingleSpecification(BasicSearch search, ApiUser apiUser) {
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
            Root<TwinLinkEntity> twinLink = query.from(TwinLinkEntity.class);
            Join<TwinLinkEntity, TwinEntity> twin = twinLink.join(TwinLinkEntity.Fields.srcTwin);
            Join<TwinEntity, TwinClassEntity> twinClass = twin.join(TwinEntity.Fields.twinClass);
//            Join<TwinLinkEntity, LinkEntity> linkClass = twinLink.join(TwinLinkEntity.Fields.link);
//            Join<LinkEntity, TwinClassEntity> twinClass = linkClass.join(LinkEntity.Fields.srcTwinClass);

            //check links
            List<Predicate> predicatesLinkListOr = new ArrayList<>();
            if(MapUtils.isNotEmpty(search.getTwinLinksMap())) {
                for (Map.Entry<UUID, Set<UUID>> linkDstTwinSet : search.getTwinLinksMap().entrySet()) {
                    if (CollectionUtils.isNotEmpty(linkDstTwinSet.getValue())) {
                        predicatesLinkListOr.add(
                                cb.and(
                                        cb.equal(twinLink.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()),
                                        twinLink.get(TwinLinkEntity.Fields.dstTwinId).in(linkDstTwinSet.getValue())
                                )
                        );
                    } else {
                        predicatesLinkListOr.add(cb.equal(twinLink.get(TwinLinkEntity.Fields.linkId), linkDstTwinSet.getKey()));
                    }
                }
            }

            Predicate predicate = cb.and(getPredicate(cb, predicatesLinkListOr, true));

            //check twin ids
            if(CollectionUtils.isNotEmpty(search.getTwinIdList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.id).in(search.getTwinIdList()));

            //check not twin ids
            if(CollectionUtils.isNotEmpty(search.getTwinIdExcludeList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.id).in(search.getTwinIdExcludeList()).not());

            //check class
            if (!CollectionUtils.isEmpty(search.getTwinClassIdList())) {
                for (UUID twinClassId : search.getTwinClassIdList()) {
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
                    predicate = cb.and(
                            predicate,
                            checkClassId,
                            cb.or(//todo - hope its not "and"...write some cases on paper
                                    cb.and(joinPredicateUserLevel, rootPredicateUser),
                                    cb.and(joinPredicateBusinessLevel, rootPredicateBusiness)
                                    //todo system level:  add Subquery to detect valid user and business account twins
                            )
                    );

                }
            } else { // no class filter, so we have to add force filtering by owner
                Predicate noClassPredicate;
                if (apiUser.isUserSpecified()) {
                    noClassPredicate = cb.and(cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerUserId), finalUserId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)
                                    )
                            )
                    );
                } else noClassPredicate = cb.and(cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)));

                if (apiUser.isBusinessAccountSpecified()) {
                    noClassPredicate = cb.and(noClassPredicate, cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)
                                    )
                            )
                    );
                } else cb.and(noClassPredicate, cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)));
                predicate = cb.and(predicate, noClassPredicate);
            }


            //check assigned user
            if(CollectionUtils.isNotEmpty(search.getAssignerUserIdList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.assignerUserId).in(search.getAssignerUserIdList()));
            //check created by user
            if(CollectionUtils.isNotEmpty(search.getCreatedByUserIdList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.createdByUserId).in(search.getCreatedByUserIdList()));

            //check twin status
            if(CollectionUtils.isNotEmpty(search.getStatusIdList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.twinStatusId).in(search.getStatusIdList()));
            //check head twin
            if(CollectionUtils.isNotEmpty(search.getHeaderTwinIdList()))
                predicate = cb.and(predicate, twin.get(TwinEntity.Fields.headTwinId).in(search.getHeaderTwinIdList()));
            return predicate;
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
