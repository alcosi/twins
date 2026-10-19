package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.math.IntegerRange;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2726,
        name = "By direct children count (given)",
        description = "Bounds the twin direct children counter (head_hierarchy_counter_direct_children) with an inclusive range")
public class TwinFinderByDirectChildrenCountGiven extends TwinFinder {
    @FeaturerParam(name = "Count from", description = "inclusive lower bound", order = 1)
    public static final FeaturerParamInt countFrom = new FeaturerParamInt("from");

    @FeaturerParam(name = "Count to", description = "inclusive upper bound", order = 2)
    public static final FeaturerParamInt countTo = new FeaturerParamInt("to");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        Integer from = countFrom.extract(properties);
        Integer to = countTo.extract(properties);
        if (from == null && to == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "at least one of from/to bounds is required");
        twinSearch.setHeadHierarchyCounterDirectChildrenRange(new IntegerRange().setFrom(from).setTo(to));
    }
}
