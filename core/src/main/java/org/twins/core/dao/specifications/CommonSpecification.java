package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.function.TriFunction;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.FunctionalUtils.defaultParallelAccumulatorOperator;
import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class CommonSpecification<T> {
    /**
     * Creates a JPA {@code Specification} to check user permissions based on a given domain, business account,
     * user details, and associated user groups. Dynamically generates joins and applies a custom SQL function
     * for permission validation.
     *
     * @param <T> the type of the entity for which the specification is created
     * @param domainId the UUID of the domain within which the permissions are being checked
     * @param businessAccountId the UUID of the business account associated with the permissions
     * @param userId the UUID of the user for whom permissions are being checked
     * @param userGroups a set of UUIDs representing the groups the user belongs to
     * @param twinEntityFieldPath an optional array of strings representing the path to navigate and join
     *                            fields for the twin entity
     * @return a JPA {@code Specification} that evaluates whether the user has the necessary permissions,
     *         based on the provided criteria
     * @throws ServiceException if there is an error during specification creation or permission validation
     */
    public static <T> Specification<T> checkPermissions(UUID domainId, UUID businessAccountId, UUID userId, Set<UUID> userGroups,String... twinEntityFieldPath) throws ServiceException {
        return (root, query, cb) -> {
            From joinTwin = getReducedRoot(root,twinEntityFieldPath);

            Expression<UUID> spaceId = joinTwin.get(TwinEntity.Fields.permissionSchemaSpaceId);
            Expression<UUID> permissionIdTwin = joinTwin.get(TwinEntity.Fields.viewPermissionId);
            Expression<UUID> permissionIdTwinClass = joinTwin.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.viewPermissionId);
            Expression<UUID> twinClassId = joinTwin.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.id);

            Predicate isAssigneePredicate = cb.equal(joinTwin.get(TwinEntity.Fields.assignerUserId), cb.literal(userId));
            Predicate isCreatorPredicate = cb.equal(joinTwin.get(TwinEntity.Fields.createdByUserId), cb.literal(userId));

            return cb.isTrue(cb.function("permission_check", Boolean.class,
                    cb.literal(domainId),
                    cb.literal(businessAccountId),
                    spaceId,
                    permissionIdTwin,
                    permissionIdTwinClass,
                    cb.literal(userId),
                    cb.literal(collectionUuidsToSqlArray(userGroups)),
                    twinClassId,
                    cb.selectCase().when(isAssigneePredicate, cb.literal(true)).otherwise(cb.literal(false)),
                    cb.selectCase().when(isCreatorPredicate, cb.literal(true)).otherwise(cb.literal(false))
            ));
        };
    }

    /**
     * Creates a JPA specification to filter entities based on a LocalDateTime field being between
     * a specified range. Dynamically generates joins as needed for the provided field path and
     * applies the filter conditions using a DataTimeRangeDTOv1 object.
     *
     * @param <T>       the type of the entity for which the specification is created
     * @param range     an object containing the starting and ending LocalDateTime values defining
     *                  the range; can be null to apply no constraints
     * @param filedPath the hierarchical path representing the fields to navigate and join,
     *                  ending with the target field
     * @return a JPA {@code Specification} matching entities where the field is between the
     *         specified range, or an unconstrained {@code Specification} if {@code range} is null
     */
    public static <T> Specification<T> checkFieldLocalDateTimeBetween(final DataTimeRangeDTOv1 range, String... filedPath) {
        if (range == null) return (root, query, cb) -> cb.conjunction();
        else return checkFieldLocalDateTimeBetween(range.from, range.to, filedPath);
    }

    /**
     * Creates a JPA specification to filter entities based on a LocalDateTime field being between a specified range.
     * It dynamically generates joins as needed for the provided field path and applies the filter conditions.
     *
     * @param <T>       the type of the entity for which the specification is created
     * @param from      the starting LocalDateTime for the range; if null, no lower bound is applied
     * @param to        the ending LocalDateTime for the range; if null, no upper bound is applied
     * @param filedPath the hierarchical path representing the fields to navigate and join, ending with the target field
     * @return a JPA {@code Specification} matching entities where the field is between the specified range,
     *         or an unconstrained {@code Specification} if both {@code from} and {@code to} are null
     */
    public static <T> Specification<T> checkFieldLocalDateTimeBetween(final LocalDateTime from, final LocalDateTime to, String... filedPath) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (from == null && to == null) return predicate;
            if (from != null) {
                Predicate tmpPredicate = predicate;
                predicate = createPredicateWithJoins(root, cb, from, (property, criteriaBuilder, filedValue) -> criteriaBuilder.and(tmpPredicate, criteriaBuilder.greaterThanOrEqualTo(property, filedValue)), filedPath);
            }
            if (to != null) {
                Predicate tmpPredicate = predicate;
                predicate = createPredicateWithJoins(root, cb, to, (property, criteriaBuilder, filedValue) -> criteriaBuilder.and(tmpPredicate, criteriaBuilder.lessThanOrEqualTo(property, filedValue)), filedPath);
            }
            return predicate;
        };
    }

    public static <T> Specification<T> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(cb.not(root.get(uuidField).in(uuids)), root.get(uuidField).isNull())
                            : cb.not(root.get(uuidField).in(uuids)))
                    : root.get(uuidField).in(uuids);
        };
    }

    /**
     * Creates a JPA specification to filter entities by verifying if a specified UUID matches the field
     * specified by the provided entity field path. The method dynamically generates joins if required
     * by the field path and applies the equality condition on the target field.
     *
     * @param <T> the type of the entity for which the specification is created
     * @param fieldValue the UUID value to be compared against the target field
     * @param domainFiledPath the hierarchical path representing the fields to navigate and join,
     * ending with the target field
     * @return a JPA {@code Specification} matching entities where the specified UUID equals the target field;
     * null-safe and returns appropriate predicates even when field paths are invalid or null
     */
    public static <T> Specification<T> checkFieldUuid(UUID fieldValue, String... domainFiledPath) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, fieldValue, (property, criteriaBuilder, filedValue) -> criteriaBuilder.equal(property, filedValue), domainFiledPath);
    }

    /**
     * Reduces inner joins for a given root entity and creates a predicate based on a comparison function.
     *
     * @param <V> the type of the value used in the comparison
     * @param srcRoot the root entity from which the joins will be created
     * @param criteriaBuilder the CriteriaBuilder used for generating predicates
     * @param filedValue the value to compare against; if null, no comparison is performed
     * @param compareFunction a function that takes a property path, a CriteriaBuilder, and a value, and returns a predicate
     * @param filedPath the hierarchical path representing the fields to navigate and join, ending with the target field
     * @return a predicate representing the comparison, or a disjunction if the input is invalid
     */
    public static <V> Predicate createPredicateWithJoins(From srcRoot, CriteriaBuilder criteriaBuilder, V filedValue, TriFunction<Path, CriteriaBuilder, V, Predicate> compareFunction, String... filedPath) {
        //No need to compare anything
        if (filedValue == null || filedPath == null || filedPath.length == 0) {
            return criteriaBuilder.disjunction();
        }
        Path propertyPath = getFildPath(srcRoot, filedPath);
        //Perform cooperation based on compareFunction
        return compareFunction.apply(propertyPath, criteriaBuilder, filedValue);
    }

    /**
     * Retrieves the property path for a specified field by navigating and joining the given field hierarchy
     * starting from the source root entity.
     *
     * @param <T>       the type of the property represented by the resulting path
     * @param srcRoot   the source root entity from which the field path navigation begins
     * @param filedPath the hierarchical path representing the fields to navigate and join,
     *                  ending with the target field name
     * @return the {@code Path} object representing the target field in the entity hierarchy
     */
    protected static <T> Path<T> getFildPath(From srcRoot , String... filedPath) {
        List<String> filedPathList = Arrays.stream(filedPath).collect(Collectors.toList());
        //Get real filedValue property name
        String filed = filedPathList.remove(filedPathList.size() - 1);
        //Get Entity that really contains filedValue property with inner joins
        From reducedRoot = getReducedRoot(srcRoot, filedPathList);
        //Get filedValue property path
        Path<T> propertyPath = reducedRoot.get(filed);
        return propertyPath;
    }

    protected static From getReducedRoot(From srcRoot, String... filedPathList) {
        return getReducedRoot(srcRoot, Arrays.asList(filedPathList));
    }
    /**
     * Reduces the source root entity by creating inner joins along the specified field path list.
     * The resulting entity will represent the deepest level in the join hierarchy.
     *
     * @param srcRoot the source root entity from which the joins will be created
     * @param filedPathList a list of field names representing the path hierarchy to navigate and join
     * @return the reduced root entity after applying the inner joins along the field path
     */
    protected static From getReducedRoot(From srcRoot, List<String> filedPathList) {
        if (filedPathList == null || filedPathList.isEmpty()) return srcRoot;
        //Get Entity that really contains property with inner joins
        From reducedRoot = filedPathList.stream().reduce(srcRoot, (from, fld) -> from.join(fld, JoinType.INNER), defaultParallelAccumulatorOperator(From.class));
        return reducedRoot;
    }


    //    Use checkFieldLikeIn
    @Deprecated
    public static <T> Specification<T> checkFieldLikeContainsIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return checkFieldLikeIn(field, CollectionUtils.isEmpty(search) ? search : search.stream().map(it -> "%" + it + "%").collect(Collectors.toSet()), not, or);
    }

    /**
     * Creates a JPA specification for filtering entities based on a field's value using a case-insensitive "like" operation.
     * Supports inclusion or exclusion of values, and combines predicates with logical AND or OR operators.
     *
     * @param <T> the type of the entity for which the specification is created
     * @param field the name of the field to be filtered
     * @param search a collection of string values to be used for the "like" comparison
     * @param not if true, negates the "like" operation, effectively creating a "not like" filter
     * @param or if true, combines the predicates using a logical OR; otherwise, combines using a logical AND
     * @return a JPA {@code Specification} representing the applied filter conditions, or {@code cb.conjunction()} if the search collection is empty
     */
    public static <T> Specification<T> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            ArrayList<Predicate> predicates = new ArrayList<>();
            for (String name : search) {
                Predicate predicate = cb.like(cb.lower(root.get(field)), name.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }
}
