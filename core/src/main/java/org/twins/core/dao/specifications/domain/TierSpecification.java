package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dto.rest.LongRangeDTOv1;

import java.util.ArrayList;
import java.util.List;

public class TierSpecification extends CommonSpecification<TierEntity> {

    public static Specification<TierEntity> empty() {
        return (root, query, cb) -> cb.conjunction();
    }


    public static Specification<TierEntity> checkAttachmentsStorageQuotaCountRange(LongRangeDTOv1 range) {
        return (root, query, cb) -> {
            if (range == null || (range.getFrom() == null && range.getTo() == null)) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            if (range.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(TierEntity.Fields.attachmentsStorageQuotaCount), range.getFrom()));
            }
            if (range.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(TierEntity.Fields.attachmentsStorageQuotaCount), range.getTo()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }


    public static Specification<TierEntity> checkAttachmentsStorageQuotaSizeRange(LongRangeDTOv1 range) {
        return (root, query, cb) -> {
            if (range == null || (range.getFrom() == null && range.getTo() == null)) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            if (range.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(TierEntity.Fields.attachmentsStorageQuotaSize), range.getFrom()));
            }
            if (range.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(TierEntity.Fields.attachmentsStorageQuotaSize), range.getTo()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }


    public static Specification<TierEntity> checkUserCountQuotaRange(LongRangeDTOv1 range) {
        return (root, query, cb) -> {
            if (range == null || (range.getFrom() == null && range.getTo() == null)) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            if (range.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(TierEntity.Fields.userCountQuota), range.getFrom()));
            }
            if (range.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(TierEntity.Fields.userCountQuota), range.getTo()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<TierEntity> checkTernary(final String field, Ternary ternary) {
        return (root, query, cb) -> {
            if (ternary == null)
                return cb.conjunction();
            return switch (ternary) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }
}