package org.twins.core.featurer.search.function;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchParamEntity;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = 25,
        name = "SearchFunction",
        description = "")
@Slf4j
public abstract class SearchFunction extends Featurer {
    public UUID getId(SearchParamEntity searchParamEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, searchParamEntity.getSearchFunctionParams(), new HashMap<>());
        return getId(properties);
    }

    public abstract UUID getId(Properties properties) throws ServiceException;

    public abstract boolean validForField(SearchField searchField);
}
