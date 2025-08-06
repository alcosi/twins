package org.twins.core.featurer.classfield.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4002,
        name = "By ordered ids",
        description = "")
public class FieldSorterByOrderedIds extends FieldSorter {
    @FeaturerParam(name = "Field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    public Sort createSort(Properties properties) throws ServiceException {
        Set<UUID> orderedIds = fieldIds.extract(properties);
        StringBuilder caseExpression = new StringBuilder("CASE ");
        int index = 0;
        for (UUID id : orderedIds) {
            caseExpression.append("WHEN id = '").append(id).append("' THEN ").append(index).append(" ");
            index++;
        }
        caseExpression.append("ELSE ").append(orderedIds.size()).append(" END");
        return JpaSort.unsafe(caseExpression.toString());
    }
}
