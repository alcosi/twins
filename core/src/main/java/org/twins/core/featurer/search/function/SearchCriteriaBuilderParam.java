package org.twins.core.featurer.search.function;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = 2501,
        name = "SearchCriteriaBuilderConfiguredId",
        description = "")
public class SearchCriteriaBuilderParam extends SearchCriteriaBuilderSingleUUID {
    @FeaturerParam(name = "paramKey", description = "")
    public static final FeaturerParamString paramKey = new FeaturerParamString("paramKey");

    @FeaturerParam(name = "required", description = "")
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramKeyStr = paramKey.extract(properties);
        String paramValue = namedParamsMap.get(paramKeyStr);
        if (StringUtils.isBlank(paramValue))
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed");
            else
                return;
        if (UuidUtils.isUUID(paramValue)) {
            super.concat(twinSearch, searchPredicateEntity, properties, namedParamsMap);
            return;
        } else if (searchPredicateEntity.getSearchField() == SearchField.twinNameLike) {
            twinSearch.addTwinNameLike(paramValue);
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");


    }

    @Override
    protected UUID getId(Properties properties, Map<String, String> namedParamsMap) {
        return UUID.fromString(namedParamsMap.get(paramKey.extract(properties))); // it's safe because of try/catch inside concat
    }
}
