package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
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
        if (mapperContext.getModeOrUse(FieldsMode.NO_FIELDS) != FieldsMode.NO_FIELDS)
            dst.fields(
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.findTwinClassFieldsIncludeParent(src), mapperContext.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.ID_KEY_ONLY))); //todo only required
        if (mapperContext.getModeOrUse(HeadTwinMode.HIDE) != HeadTwinMode.HIDE && src.getHeadTwinClassId() != null)
            dst.validHeads(
                    twinRestDTOMapper.convertList(
                            twinService.findTwinsByClassId(src.getHeadTwinClassId()), mapperContext.setModeIfNotPresent(TwinBaseRestDTOMapper.TwinMode.ID_NAME_ONLY)));
        if (mapperContext.getModeOrUse(LinksMode.HIDE) != LinksMode.HIDE) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassBaseRestDTOMapper.ClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }

    public enum FieldsMode implements MapperMode {
        NO_FIELDS, ALL_FIELDS, ONLY_REQUIRED;
        public static final String _NO_FIELDS = "NO_FIELDS";
        public static final String _ALL_FIELDS = "ALL_FIELDS";
        public static final String _ONLY_REQUIRED = "ONLY_REQUIRED";
    }

    public enum HeadTwinMode implements MapperMode {
        SHOW, HIDE;
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }

    public enum LinksMode implements MapperMode {
        SHOW, HIDE;
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }
}
