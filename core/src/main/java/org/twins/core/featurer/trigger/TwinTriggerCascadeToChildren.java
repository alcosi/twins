package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinSearchService;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1508,
        name = "CascadeToChildren",
        description = "Cascade destination status to head-based children")
@RequiredArgsConstructor
public class TwinTriggerCascadeToChildren extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Max cascade depth (1 = direct children only, null = unlimited)", optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinSearchService twinSearchService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        log.info("Running CascadeToChildren: twin {}, destination status '{}', depth: {}", twinEntity.logShort(), dstTwinStatus.getKey(), depthValue);

        var search = new BasicSearch();
        search.setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new HierarchySearch()
                        .setDepth(depthValue)
                        .setIdList(Set.of(twinEntity.getId()))
        );
        List<TwinEntity> twinsToCascade = twinSearchService.findTwins(search);

        if (!twinsToCascade.isEmpty()) {
            twinService.changeStatus(twinsToCascade, dstTwinStatus);
        }
    }
}
