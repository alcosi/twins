package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryNotificationRecipientCollectorMode;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryNotificationRecipientMode;
import org.twins.core.service.notification.HistoryNotificationRecipientCollectorService;

import java.util.Collection;

@RequiredArgsConstructor
@MapperModeBinding(modes = HistoryNotificationRecipientCollectorMode.class)
@Component
public class HistoryNotificationRecipientCollectorDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorEntity, HistoryNotificationRecipientCollectorDTOv1> {
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;
    @MapperModePointerBinding(modes = FeaturerMode.HistoryNotificationRecipientCollector2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final HistoryNotificationRecipientCollectorService historyNotificationRecipientCollectorService;

    @Override
    public void map(HistoryNotificationRecipientCollectorEntity src, HistoryNotificationRecipientCollectorDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryNotificationRecipientCollectorMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setRecipientId(src.getHistoryNotificationRecipientId())
                            .setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId())
                            .setRecipientResolverParams(src.getRecipientResolverParams())
                            .setExclude(src.getExclude());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setRecipientId(src.getHistoryNotificationRecipientId())
                            .setExclude(src.getExclude());
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientMode.HistoryNotificationRecipientCollector2HistoryNotificationRecipientMode.HIDE)) {
            dst.setRecipientId(src.getHistoryNotificationRecipientId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipientEntity(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientMode.HistoryNotificationRecipientCollector2HistoryNotificationRecipientMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FeaturerMode.HistoryNotificationRecipientCollector2FeaturerMode.HIDE)) {
            dst.setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId());
            historyNotificationRecipientCollectorService.loadRecipientResolverFeaturer(src);
            featurerRestDTOMapper.postpone(src.getRecipientResolverFeaturer(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.HistoryNotificationRecipientCollector2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<HistoryNotificationRecipientCollectorEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FeaturerMode.HistoryNotificationRecipientCollector2FeaturerMode.HIDE)) {
            historyNotificationRecipientCollectorService.loadRecipientResolverFeaturer(srcCollection);
        }
    }
}
