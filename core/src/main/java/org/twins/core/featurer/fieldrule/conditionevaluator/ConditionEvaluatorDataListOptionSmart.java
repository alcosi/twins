package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;
import org.twins.core.service.datalist.DataListOptionSearchService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_4504,
        name = "Condition evaluator by option external id (smart)",
        description = "Evaluates a basic condition against a selector field option external id")
public class ConditionEvaluatorDataListOptionSmart extends ConditionEvaluator<ConditionDescriptorValue> {
    @Autowired
    DataListOptionSearchService dataListOptionSearchService;

    @Autowired
    TwinClassFieldService twinClassFieldService;

    @Override
    protected ConditionDescriptorValue getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException {
        loadConditionDescriptors(List.of(twinClassFieldConditionEntity));
        return (ConditionDescriptorValue) twinClassFieldConditionEntity.getConditionDescriptor();
    }

    @Override
    public void loadConditionDescriptors(List<TwinClassFieldConditionEntity> twinClassFieldConditionList) throws ServiceException {
        var dataListOptionSearch = new DataListOptionSearch();
        Properties properties;
        var baseTwinClassFieldIds = twinClassFieldConditionList.stream().map(TwinClassFieldConditionEntity::getBaseTwinClassFieldId).collect(Collectors.toSet());
        var fieldToDataListMap = twinClassFieldService.toDataListMap(baseTwinClassFieldIds);
        var groupedConditions = new HashMap<String, List<TwinClassFieldConditionEntity>>();
        //we will try to bulk convert external ids to option ids
        for (var condition : twinClassFieldConditionList) {
            properties = featurerService.extractProperties(this, condition.getConditionEvaluatorParams());
            var externalId = valueToCompareWith.extract(properties);
            var dataListId = fieldToDataListMap.get(condition.getBaseTwinClassFieldId());
            dataListOptionSearch
                    .addDataListId(dataListId, false)
                    .addExternalId(externalId,false); // we cannot create a search for pairs datalist -> externalId, but we will trick it later
            groupedConditions.computeIfAbsent(dataListId + externalId, k -> new ArrayList<>()).add(condition);
            condition.setConditionDescriptor(new ConditionDescriptorValue()
                    .conditionOperator(conditionOperator.extract(properties)));
        }
        var options = dataListOptionSearchService.findDataListOptions(dataListOptionSearch);
        for (var option : options) {
            var conditions = groupedConditions.get(option.getDataListId() + option.getExternalId());
            if (CollectionUtils.isEmpty(conditions))
                continue;
            for (var condition : conditions) {
                condition.getConditionDescriptor().valueToCompareWith(option.getId().toString());
            }
        }
    }
}
