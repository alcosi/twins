package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.hibernate.query.SortDirection;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.ValueBindJpaCriteriaParameter;
import org.hibernate.query.sqm.tree.predicate.SqmComparisonPredicate;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.*;
import java.util.function.Function;

@FeaturerType(id = FeaturerTwins.TYPE_41,
        name = "Twin Search Sorter",
        description = "Order twin search")
@Slf4j
public abstract class TwinSorter extends FeaturerTwins {

    @FeaturerParam(name = "NullsLast", description = "", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean nullsLast = new FeaturerParamBoolean("nullsLast");

    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(HashMap<String, String> twinSorterParams, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinSorterParams);
        if (!checkCompatibleSorter(featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class)))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_SORTER_IS_INCOMPATIBLE, "The field typer " + twinClassFieldEntity.getFieldTyperFeaturerId() + " is not compatible with the sorter " + twinClassFieldEntity.getTwinSorterFeaturerId() + "; " + twinClassFieldEntity.logShort());

        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties, twinClassFieldEntity, direction);
    }

    public abstract boolean checkCompatibleSorter(FieldTyper fieldTyper) throws ServiceException;

    public abstract Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException;


    /**
     * Adds sorting by value (ASC or DESC) and combines with existing query orders.
     *
     * @param orders    The list of Order clauses to append to
     * @param cb        The CriteriaBuilder
     * @param query     The CriteriaQuery
     * @param value     The expression to sort by
     * @param direction The sort direction (ASC or DESC)
     */
    protected void addValueAndCombineOrders(List<Order> orders, CriteriaBuilder cb, CriteriaQuery<?> query, Expression<?> value, SortDirection direction) {
        if (direction.equals(SortDirection.DESCENDING)) {
            orders.add(cb.desc(value));
        } else {
            orders.add(cb.asc(value));
        }
        // Combine with existing orders
        List<Order> current = new ArrayList<>(query.getOrderList());
        current.addAll(orders);
        query.orderBy(current);
    }

    protected void addNullsPositionOrder(List<Order> orders, CriteriaBuilder cb, Join<TwinEntity, ?> tfJoin, String field, Properties properties) {
        boolean isNullsLast = nullsLast.extract(properties);
        if (isNullsLast) {
            orders.add(cb.asc(cb.selectCase().when(cb.isNull(tfJoin.get(field)), 1).otherwise(0)));
        } else {
            orders.add(cb.desc(cb.selectCase().when(cb.isNull(tfJoin.get(field)), 1).otherwise(0)));
        }
    }

    protected void addNullsPositionOrder(List<Order> orders, CriteriaBuilder cb, Path<TwinEntity> root, String field, Properties properties) {
        boolean isNullsLast = nullsLast.extract(properties);
        if (isNullsLast) {
            orders.add(cb.asc(cb.selectCase().when(cb.isNull(root.get(field)), 1).otherwise(0)));
        } else {
            orders.add(cb.desc(cb.selectCase().when(cb.isNull(root.get(field)), 1).otherwise(0)));
        }
    }
}
