package org.twins.core.featurer.fieldfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3204,
        name = "Optional fields",
        description = "")
public class FieldFinderOptional extends FieldFinder {

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch) throws ServiceException {
        fieldSearch.setRequired(Ternary.ONLY_NOT);
    }
}