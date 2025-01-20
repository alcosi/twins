package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.function.TriFunction;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.FunctionalUtils.defaultParallelAccumulatorOperator;
import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class CommonSpecification<T> {

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
     * @param domainId the UUID value to be compared against the target field
     * @param domainFiledPath the hierarchical path representing the fields to navigate and join,
     * ending with the target field
     * @return a JPA {@code Specification} matching entities where the specified UUID equals the target field;
     * null-safe and returns appropriate predicates even when field paths are invalid or null
     */
    public static <T> Specification<T> checkFieldUuid(UUID domainId, String... domainFiledPath) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.equal(property, filedValue), domainFiledPath);
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
        List<String> domainFiledPathList = Arrays.stream(filedPath).collect(Collectors.toList());
        //Get real filedValue property name
        String domainFiled = domainFiledPathList.remove(domainFiledPathList.size() - 1);
        //Get Entity that really contains filedValue property with inner joins
        From reducedRoot = domainFiledPathList.stream().reduce(srcRoot, (from, filed) -> from.join(filed, JoinType.INNER), defaultParallelAccumulatorOperator(From.class));
        //Get filedValue property path
        Path propertyPath = reducedRoot.get(domainFiled);
        //Perform cooperation based on compareFunction
        return compareFunction.apply(propertyPath, criteriaBuilder, filedValue);
    }


    //    Use checkFieldLikeIn
    @Deprecated
    public static <T> Specification<T> checkFieldLikeContainsIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return checkFieldLikeIn(field, search.stream().map(it -> "%" + it + "%").collect(Collectors.toSet()), not, or);
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
