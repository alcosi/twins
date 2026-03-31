package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionMode.class)
public class TransitionBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv1> {
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final PermissionService permissionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasMode(LinkMode.MANAGED) && !permissionService.currentUserHasPermission(Permissions.TRANSITION_MANAGE))
            throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + LinkMode.MANAGED + "] is not allowed for current user");
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case DETAILED, MANAGED ->
                dst
                        .setId(src.getId())
                        .setDstTwinStatusId(src.getDstTwinStatusId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setAllowComment(src.isAllowComment())
                        .setAllowAttachments(src.isAllowAttachment())
                        .setAllowLinks(src.isAllowLinks())
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setType(src.getTwinflowTransitionTypeId());
            case SHORT ->
                dst
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setAlias(src.getTwinflowTransitionAlias().getAlias())
                        .setId(src.getId());
        }
        if (mapperContext.hasModeButNot(StatusMode.Transition2StatusMode.HIDE)) {
            dst.setDstTwinStatusId(src.getDstTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getDstTwinStatus(), mapperContext.forkOnPoint(StatusMode.Transition2StatusMode.SHORT));
        }
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
