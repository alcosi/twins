package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
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
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperProperties);
        if (mapperProperties.getModeOrUse(FieldsMode.NO_FIELDS) != FieldsMode.NO_FIELDS)
            dst.fields(
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.findTwinClassFieldsIncludeParent(src), mapperProperties.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.ID_KEY_ONLY))); //todo only required
        if (mapperProperties.getModeOrUse(HeadTwinMode.HIDE) != HeadTwinMode.HIDE && src.getHeadTwinClassId() != null)
            dst.validHeads(
                    twinRestDTOMapper.convertList(
                            twinService.findTwinsByClassId(src.getHeadTwinClassId()), mapperProperties.setModeIfNotPresent(TwinRestDTOMapper.TwinMode.ID_NAME_ONLY)));
        if (mapperProperties.getModeOrUse(LinksMode.HIDE) != LinksMode.HIDE) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkList(linkForwardRestDTOMapper.convertList(findTwinClassLinksResult.getForwardLinks(), mapperProperties))
                    .backwardLinkList(linkBackwardRestDTOMapper.convertList(findTwinClassLinksResult.getBackwardLinks(), mapperProperties));
        }
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
