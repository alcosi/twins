package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamStringSet;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2709,
        name = "Field text",
        description = "")
public class SearchCriteriaBuilderFieldText extends SearchCriteriaBuilder {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Value like all of list", description = "", optional = true, order = 2)
    public static final FeaturerParamStringSet valueLikeAllOfList = new FeaturerParamStringSet("valueLikeAllOfList");

    @FeaturerParam(name = "Value like any of list", description = "", optional = true, order = 3)
    public static final FeaturerParamStringSet valueLikeAnyOfList = new FeaturerParamStringSet("valueLikeAnyOfList");

    @FeaturerParam(name = "Value like no all of list", description = "", optional = true, order = 4)
    public static final FeaturerParamStringSet valueLikeNoAllOfList = new FeaturerParamStringSet("valueLikeNoAllOfList");

    @FeaturerParam(name = "Value like no all of list", description = "", optional = true, order = 5)
    public static final FeaturerParamStringSet valueLikeNoAnyOfList = new FeaturerParamStringSet("valueLikeNoAnyOfList");

    private final TwinClassFieldService twinClassFieldService;

    public SearchCriteriaBuilderFieldText(TwinClassFieldService twinClassFieldService) {
        super();
        this.twinClassFieldService = twinClassFieldService;
    }

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID paramTwinClassFieldId = twinClassFieldId.extract(properties);
        Set<String> paramValueLikeAll = valueLikeAllOfList.extract(properties);
        Set<String> paramValueLikeAny = valueLikeAnyOfList.extract(properties);
        Set<String> paramValueLikeNoAll = valueLikeNoAllOfList.extract(properties);
        Set<String> paramValueLikeNoAny = valueLikeNoAnyOfList.extract(properties);

        if (searchPredicateEntity.getSearchField() != SearchField.fieldText)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(paramTwinClassFieldId);

        twinSearch.addField(
                new TwinFieldSearchText()
                        .setValueLikeAllOfList(CollectionUtils.isNotEmpty(paramValueLikeAll) ? paramValueLikeAll : null)
                        .setValueLikeAnyOfList(CollectionUtils.isNotEmpty(paramValueLikeAny) ? paramValueLikeAny : null)
                        .setValueLikeNoAllOfList(CollectionUtils.isNotEmpty(paramValueLikeNoAll) ? paramValueLikeNoAll : null)
                        .setValueLikeNoAnyOfList(CollectionUtils.isNotEmpty(paramValueLikeNoAny) ? paramValueLikeNoAny : null)
                        .setTwinClassFieldEntity(twinClassField)
                        .setFieldTyper(featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class))
        );
    }
}
