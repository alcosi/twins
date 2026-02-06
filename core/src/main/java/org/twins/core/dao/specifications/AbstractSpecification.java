package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cambium.common.util.FunctionalUtils.defaultParallelAccumulatorOperator;

abstract class AbstractSpecification<T> {

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
    public static <V> Predicate createPredicateWithJoins(From srcRoot, CriteriaBuilder criteriaBuilder, V filedValue, TriFunction<Path, CriteriaBuilder, V, Predicate> compareFunction,JoinType joinType, String... filedPath) {
        //No need to compare anything
        if (filedValue == null || filedPath == null || filedPath.length == 0) {
            return criteriaBuilder.disjunction();
        }
        Path propertyPath = getFieldPath(srcRoot,joinType, filedPath);
        //Perform cooperation based on compareFunction
        return compareFunction.apply(propertyPath, criteriaBuilder, filedValue);
    }

    /**
     * Retrieves the path to a specific property within an entity, using the specified root entity,
     * join type, and a hierarchical path of field names.
     *
     * @param <T> the type of the property represented by the resulting path
     * @param srcRoot the root entity from which the field path starts
     * @param joinType the type of join (e.g., INNER, LEFT, RIGHT) used for navigating the field path
     * @param filedPath the hierarchical path of field names leading to the target property
     * @return the path representing the specified property within the entity structure
     */
    protected static <T> Path<T> getFieldPath(From srcRoot, JoinType joinType , String... filedPath) {
        List<String> filedPathList = Arrays.stream(filedPath).collect(Collectors.toList());
        //Get real filedValue property name
        String filed = filedPathList.remove(filedPathList.size() - 1);
        //Get Entity that really contains filedValue property with inner joins
        From reducedRoot = getReducedRoot(srcRoot,joinType, filedPathList);
        //Get filedValue property path
        Path<T> propertyPath = reducedRoot.get(filed);
        return propertyPath;
    }

    /**
     * Reduces the source root entity by creating joins based on the specified field path list
     * and join type. This method delegates the join creation to another implementation by
     * converting the field path array into a list.
     *
     * @param srcRoot the source root entity from which the joins will be created
     * @param joinType the type of join (e.g., INNER, LEFT, RIGHT) to be applied while navigating the field path
     * @param filedPathList an array of field names representing the path hierarchy to navigate and join
     * @return the reduced root entity after applying the joins along the field path
     */
    protected static From getReducedRoot(From srcRoot,JoinType joinType, String... filedPathList) {
        return getReducedRoot(srcRoot, joinType,Arrays.asList(filedPathList));
    }


    /**
     * Reduces the source root entity by creating joins based on the specified field path list
     * and join type. The resulting entity will represent the deepest level in the join hierarchy.
     *
     * @param srcRoot the source root entity from which the joins will be created
     * @param filedPathList a list of field names representing the path hierarchy to navigate and join
     * @param joinType the type of join (e.g., INNER, LEFT, RIGHT) to be applied while navigating the field path
     * @return the reduced root entity after applying the joins along the field path
     */
    protected static From getReducedRoot(From srcRoot,JoinType joinType, List<String> filedPathList) {
        if (filedPathList == null || filedPathList.isEmpty()) return srcRoot;
        //Get Entity that really contains property with inner joins
        From reducedRoot = filedPathList.stream().reduce(srcRoot, (from, fld) -> from.join(fld, joinType), defaultParallelAccumulatorOperator(From.class));
        return reducedRoot;
    }

    /**
     * Gets an existing join or creates a new one if it doesn't exist.
     * This prevents duplicate joins to the same table.
     *
     * @param from the source From object
     * @param attribute the attribute name to join
     * @param joinType the type of join
     * @return existing or newly created Join
     */
    protected static Join<?, ?> getOrCreateJoin(From<?, ?> from, String attribute, JoinType joinType) {
        // Check if join already exists by attribute name
        for (Join<?, ?> join : from.getJoins()) {
            Attribute<?, ?> attr = (Attribute<?, ?>) join.getAttribute();
            if (attr != null && attr.getName().equals(attribute)) {
                return join;
            }
        }
        return from.join(attribute, joinType);
    }

}
