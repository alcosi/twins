package org.twins.core.mappers.rest.twinflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinTransitionRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinTransitionViewDTOv1> {
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final I18nService i18nService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinTransitionViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setDstTwinStatusId(src.getDstTwinStatusId())
                        .setDstTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getDstTwinStatus(), mapperContext))
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setAllowComment(src.isAllowComment())
                        .setAllowAttachments(src.isAllowAttachment())
                        .setAllowLinks(src.isAllowLinks())
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setId(src.getId());
                break;
            case SHORT:
                dst
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setId(src.getId());
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
