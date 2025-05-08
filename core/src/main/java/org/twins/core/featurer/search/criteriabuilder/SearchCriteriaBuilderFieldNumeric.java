package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchField;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinFieldSearchNumeric;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2706,
        name = "Field numeric",
        description = "")
public class SearchCriteriaBuilderFieldNumeric extends SearchCriteriaBuilder {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Less then", description = "", optional = true, order = 2)
    public static final FeaturerParamString lessThen = new FeaturerParamString("lessThen");

    @FeaturerParam(name = "More then", description = "", optional = true, order = 3)
    public static final FeaturerParamString moreThen = new FeaturerParamString("moreThen");

    @FeaturerParam(name = "Equals", description = "", optional = true, order = 4)
    public static final FeaturerParamString equals = new FeaturerParamString("equals");

    private final TwinClassFieldService twinClassFieldService;

    public SearchCriteriaBuilderFieldNumeric(TwinClassFieldService twinClassFieldService) {
        super();
        this.twinClassFieldService = twinClassFieldService;
    }

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID paramTwinClassFieldId = twinClassFieldId.extract(properties);
        String paramLessThen = lessThen.extract(properties);
        String paramMoreThen = moreThen.extract(properties);
        String paramEquals = equals.extract(properties);
        Double less = null;
        Double more = null;
        Double equals = null;

        if (StringUtils.isNotBlank(paramLessThen)) {
            try {
                less = Double.parseDouble(paramLessThen);
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for LT compare with field: [" + paramLessThen + "]");
            }
        }
        if (StringUtils.isNotBlank(paramMoreThen)) {
            try {
                more = Double.parseDouble(paramMoreThen);
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for LT compare with field: [" + paramMoreThen + "]");
            }
        }
        if (StringUtils.isNotBlank(paramEquals)) {
            try {
                equals = Double.parseDouble(paramEquals);
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for LT compare with field: [" + paramEquals + "]");
            }
        }

        if (searchPredicateEntity.getSearchField() != SearchField.fieldNumeric)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(paramTwinClassFieldId);

        twinSearch.addField(
                new TwinFieldSearchNumeric()
                        .setLessThen(less)
                        .setMoreThen(more)
                        .setEquals(equals)
                        .setTwinClassFieldEntity(twinClassField)
                        .setFieldTyper(featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class))
        );
    }
}
