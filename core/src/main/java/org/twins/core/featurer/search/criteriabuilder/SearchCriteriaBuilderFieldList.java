package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinFieldSearchList;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2710,
        name = "Field list",
        description = "")
public class SearchCriteriaBuilderFieldList extends SearchCriteriaBuilder {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Option all of list", description = "", optional = true, order = 2)
    public static final FeaturerParamUUIDSet optionsAllOfList = new FeaturerParamUUIDSetTwinsTwinClassFieldId("optionsAllOfList");

    @FeaturerParam(name = "Options any of list", description = "", optional = true, order = 3)
    public static final FeaturerParamUUIDSet optionsAnyOfList = new FeaturerParamUUIDSetTwinsTwinClassFieldId("optionsAnyOfList");

    @FeaturerParam(name = "Options no all of list", description = "", optional = true, order = 4)
    public static final FeaturerParamUUIDSet optionsNoAllOfList = new FeaturerParamUUIDSetTwinsTwinClassFieldId("optionsNoAllOfList");

    @FeaturerParam(name = "options no any of list", description = "", optional = true, order = 5)
    public static final FeaturerParamUUIDSet optionsNoAnyOfList = new FeaturerParamUUIDSetTwinsTwinClassFieldId("optionsNoAnyOfList");

    private final TwinClassFieldService twinClassFieldService;

    public SearchCriteriaBuilderFieldList(TwinClassFieldService twinClassFieldService) {
        super();
        this.twinClassFieldService = twinClassFieldService;
    }

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID paramTwinClassFieldId = twinClassFieldId.extract(properties);
        Set<UUID> paramOptionsAll = optionsAllOfList.extract(properties);
        Set<UUID> paramOptionsAny = optionsAnyOfList.extract(properties);
        Set<UUID> paramOptionsNoAll = optionsNoAllOfList.extract(properties);
        Set<UUID> paramOptionsNoAny = optionsNoAnyOfList.extract(properties);

        if (searchPredicateEntity.getSearchField() != SearchField.fieldList)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(paramTwinClassFieldId);

        twinSearch.addField(
                new TwinFieldSearchList()
                        .setOptionsAllOfList(CollectionUtils.isNotEmpty(paramOptionsAll) ? paramOptionsAll : null)
                        .setOptionsAnyOfList(CollectionUtils.isNotEmpty(paramOptionsAny) ? paramOptionsAny : null)
                        .setOptionsNoAllOfList(CollectionUtils.isNotEmpty(paramOptionsNoAll) ? paramOptionsNoAll : null)
                        .setOptionsNoAnyOfList(CollectionUtils.isNotEmpty(paramOptionsNoAny) ? paramOptionsNoAny : null)
                        .setTwinClassFieldEntity(twinClassField)
                        .setFieldTyper(featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class))
        );
    }
}
