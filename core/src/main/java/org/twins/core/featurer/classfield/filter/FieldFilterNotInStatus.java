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
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3603,
        name = "Filter field by twin status ids",
        description = "")
public class FieldFilterNotInStatus extends FieldFilter {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Override
    public void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> unfilteredFieldsKit, TwinEntity twin, List<TwinClassFieldEntity> fields) throws ServiceException {
        if (statusIds.extract(properties).contains(twin.getTwinStatusId())) {
            unfilteredFieldsKit.addAll(fields);
        }
    }
}