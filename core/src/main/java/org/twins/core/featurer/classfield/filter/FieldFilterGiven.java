package org.twins.core.featurer.classfield.filter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3601,
        name = "Filter given field by id",
        description = "")
public class FieldFilterGiven extends FieldFilter {
    @FeaturerParam(name = "Filtered field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet filteredFieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("filteredFieldIds");

    @Override
    public void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> unfilteredFieldsKit, TwinEntity twin, List<TwinClassFieldEntity> fields) throws ServiceException {
        Set<UUID> excludedFieldIds = filteredFieldIds.extract(properties);

        for (TwinClassFieldEntity field : fields) {
            if (!excludedFieldIds.contains(field.getId())) {
                unfilteredFieldsKit.add(field);
            }
        }
    }
}
