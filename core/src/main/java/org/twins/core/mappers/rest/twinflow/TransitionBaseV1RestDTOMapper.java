package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TransitionBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv1> {
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final I18nService i18nService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.TransitionMode.SHORT)) {
            case DETAILED:
                dst
                        .setDstTwinStatusId(src.getDstTwinStatusId())
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
        if (mapperContext.hasModeButNot(MapperMode.TransitionStatusMode.HIDE))
            dst
                    .setDstTwinStatusId(src.getDstTwinStatusId())
                    .setDstTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getDstTwinStatus(), mapperContext.forkOnPoint(MapperMode.TransitionStatusMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.TransitionMode.HIDE);
    }

}