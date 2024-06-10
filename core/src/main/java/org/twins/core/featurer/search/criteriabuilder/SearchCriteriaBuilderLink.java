package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@Featurer(id = 2703,
        name = "SearchCriteriaBuilderLink",
        description = "")
public class SearchCriteriaBuilderLink extends SearchCriteriaBuilder {
    @FeaturerParam(name = "linkId", description = "")
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "dstTwinId", description = "")
    public static final FeaturerParamUUIDSet dstTwinId = new FeaturerParamUUIDSetTwinsTwinId("dstTwinId");

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        if (searchPredicateEntity.getSearchField() != SearchField.linkId)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        twinSearch.addLinkDstTwinsId(linkId.extract(properties), dstTwinId.extract(properties), searchPredicateEntity.isExclude());
    }
}
