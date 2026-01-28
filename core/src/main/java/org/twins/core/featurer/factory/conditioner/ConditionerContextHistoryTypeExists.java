package org.twins.core.featurer.factory.conditioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamStringHistoryType;
import org.twins.core.service.history.HistoryService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2441,
        name = "History type exists",
        description = "History type for twin change task factories")
@Slf4j
@RequiredArgsConstructor
public class ConditionerContextHistoryTypeExists extends Conditioner {
    private final HistoryService historyService;

    @FeaturerParam(name = "History type", description = "", order = 1)
    public static final FeaturerParamStringHistoryType historyType = new FeaturerParamStringHistoryType("historyType");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return historyService.findDistinctHistoryTypesByBatchId(factoryItem.getFactoryContext().getRequestId()).contains(historyType.extract(properties));
    }
}
