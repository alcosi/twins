package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final I18nService i18nService;
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;
    final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;
    final LinkService linkService;
    @Autowired
    TwinRestDTOMapper twinRestDTOMapper;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperContext);
        if (!twinClassFieldRestDTOMapper.hideMode(mapperContext))
            dst.fields(
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.findTwinClassFieldsIncludeParent(src), mapperContext.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.SHORT))); //todo only required
        if (!mapperContext.hasMode(HeadTwinMode.HIDE) && src.getHeadTwinClassId() != null)
            dst.validHeads(
                    twinRestDTOMapper.convertList(
                            twinService.findTwinsByClassId(src.getHeadTwinClassId()), mapperContext.setModeIfNotPresent(TwinBaseRestDTOMapper.TwinMode.SHORT)));
        if (!linkForwardRestDTOMapper.hideMode(mapperContext)) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext));
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
}
