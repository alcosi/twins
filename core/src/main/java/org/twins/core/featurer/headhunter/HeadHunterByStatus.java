package org.twins.core.featurer.headhunter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2602,
        name = "By status",
        description = "")
public class HeadHunterByStatus extends HeadHunter {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Exclude status input", description = "")
    public static final FeaturerParamBoolean excludeStatusInput = new FeaturerParamBoolean("excludeStatusInput");

    @Override
    protected void expandValidHeadSearch(Properties properties, TwinClassEntity twinClassEntity, BasicSearch basicSearch) throws ServiceException {
        basicSearch
                .addStatusId(statusIds.extract(properties), excludeStatusInput.extract(properties));
    }

    @Override
    protected boolean isCreatableChildClass(Properties properties, TwinEntity twinEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        if (excludeStatusInput.extract(properties))
            return !statusIds.extract(properties).contains(twinEntity.getTwinStatusId());
        else
            return statusIds.extract(properties).contains(twinEntity.getTwinStatusId());
    }
}
