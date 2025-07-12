package org.twins.core.featurer.fieldfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3203,
        name = "Required fields",
        description = "")
public class FieldFinderRequired extends FieldFinder {
    @FeaturerParam(name = "Exclude required fields", description = "", defaultValue = "true")
    public static final FeaturerParamBoolean onlyRequired = new FeaturerParamBoolean("onlyRequired");

    @Override
    protected void createSearch(Properties properties, UUID twinClassId, TwinClassFieldSearch fieldSearch) throws ServiceException {
        if (onlyRequired.extract(properties)) {
            fieldSearch.setRequired(Ternary.ONLY);
        } else {
            fieldSearch.setRequired(Ternary.ONLY_NOT);
        }
    }
}
