package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final TwinHeadService twinHeadService;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;
    final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinStatusService twinStatusService;
    final LinkService linkService;
    @Lazy
    @Autowired
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperContext);
        if (!twinClassFieldRestDTOMapper.hideMode(mapperContext))
            dst.fields(
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.findTwinClassFieldsIncludeParent(src), mapperContext.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.SHORT))); //todo only required
        if (!mapperContext.hasMode(HeadTwinMode.HIDE) && src.getHeadTwinClassId() != null)
            dst.validHeads(
                    twinBaseRestDTOMapper.convertList(
                            twinHeadService.findValidHeads(src), mapperContext.setModeIfNotPresent(TwinBaseRestDTOMapper.TwinMode.SHORT)));
        if (!linkForwardRestDTOMapper.hideMode(mapperContext)) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext));
        }
        if (!mapperContext.hasMode(StatusMode.HIDE)) {
            List<TwinStatusEntity> validTwinClassStatusList = twinStatusService.findByTwinClass(src);
            if (validTwinClassStatusList != null) {
                Map<UUID, TwinStatusEntity> map = validTwinClassStatusList.stream().collect(Collectors.toMap(TwinStatusEntity::getId, Function.identity(), (left, right) -> left,
                        LinkedHashMap::new));
                dst
                        .statusMap(twinStatusRestDTOMapper.convertMap(map, mapperContext));
            }
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinClassBaseRestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }

    public enum HeadTwinMode implements MapperMode {
        SHOW, HIDE;
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }

    public enum StatusMode implements MapperMode {
        SHOW, HIDE;
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }
}
