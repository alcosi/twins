package org.twins.core.featurer.fieldfilter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3602,
        name = "Filter field by twin status ids",
        description = "")
public class FieldFilterStatus extends FieldFilter {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Exclude", description = "", order = 2)
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    public void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> fieldsKit, TwinEntity twin) throws ServiceException {
        if (statusIds.extract(properties).contains(twin.getTwinStatusId()) == exclude.extract(properties)) {
            fieldsKit.clear();
        }
    }
}
