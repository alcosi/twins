package org.twins.core.featurer.classfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3905,
        name = "Class finder by segment flag",
        description = "")
public class ClassFinderSegment extends ClassFinder {

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    protected void concatSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException {
        Ternary ternaryValue = exclude.extract(properties) ? Ternary.ONLY_NOT : Ternary.ONLY;
        classSearch.setSegment(ternaryValue);
    }
}
