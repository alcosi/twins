package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2705,
        name = "SearchCriteriaBuilderParamLinkDst",
        description = "")
public class SearchCriteriaBuilderParamLinkDst extends SearchCriteriaBuilder {

    @FeaturerParam(name = "paramKey", description = "")
    public static final FeaturerParamString paramKey = new FeaturerParamString("paramKey");

    @FeaturerParam(name = "linkId", description = "")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "required", description = "")
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramKeyStr = paramKey.extract(properties);
        if (null == namedParamsMap || StringUtils.isBlank(namedParamsMap.get(paramKeyStr)))
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed but required");
            else
                return;
        if (!UuidUtils.isUUID(namedParamsMap.get(paramKeyStr)))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_INCORRECT, "search param[" + paramKeyStr + "] incorrect(uuid)");

        if (searchPredicateEntity.getSearchField() != SearchField.linkId)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        twinSearch.addLinkDstTwinsId(linkId.extract(properties), List.of(UUID.fromString(namedParamsMap.get(paramKeyStr))), searchPredicateEntity.isExclude());
    }
}
