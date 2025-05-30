package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.twins.core.service.i18n.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionMode.class)
public class TransitionBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv1> {

    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case DETAILED, MANAGED ->
                dst
                        .setId(src.getId())
                        .setDstTwinStatusId(src.getDstTwinStatusId())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(i18nService.translateToLocale(src.getDescriptionI18NId()))
                        .setAllowComment(src.isAllowComment())
                        .setAllowAttachments(src.isAllowAttachment())
                        .setAllowLinks(src.isAllowLinks())
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setType(src.getTwinflowTransitionTypeId());
            case SHORT ->
                dst
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setId(src.getId());
        }
        if (mapperContext.hasModeButNot(StatusMode.Transition2StatusMode.HIDE))
            dst
                    .setDstTwinStatusId(src.getDstTwinStatusId())
                    .setDstTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getDstTwinStatus(), mapperContext.forkOnPoint(StatusMode.Transition2StatusMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
