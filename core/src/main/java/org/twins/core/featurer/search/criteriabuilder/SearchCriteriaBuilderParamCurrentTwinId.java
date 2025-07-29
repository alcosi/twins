package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2706,
        name = "Current twin id from Param",
        description = "")
public class SearchCriteriaBuilderParamCurrentTwinId extends SearchCriteriaBuilderSingleUUID {
    public static final String PARAM_CURRENT_TWIN_ID = "currentTwinId";

    @FeaturerParam(name = "Required", description = "", optional = true, defaultValue = "true", order = 1)
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramValue = namedParamsMap.get(PARAM_CURRENT_TWIN_ID);
        if (StringUtils.isBlank(paramValue))
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + PARAM_CURRENT_TWIN_ID + "] missed");
            else
                return;
        if (!UuidUtils.isUUID(paramValue)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        }
        super.concat(twinSearch, searchPredicateEntity, properties, namedParamsMap);
    }

    @Override
    protected UUID getId(Properties properties, Map<String, String> namedParamsMap) {
        return UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_ID)); // it's safe because of try/catch inside concat
    }
}
