package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv3;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserService;

@Component
@RequiredArgsConstructor
public class LinkForwardRestDTOV3Mapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv3> {

    private final LinkForwardRestDTOV2Mapper linkForwardRestDTOV2Mapper;

    @MapperModePointerBinding(modes = TwinClassMode.LinkSrc2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.Link2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    private final UserService userService;

    private final PermissionService permissionService;

    @Override
    public void map(LinkEntity src, LinkDTOv3 dst, MapperContext mapperContext) throws Exception {
        linkForwardRestDTOV2Mapper.map(src, dst, mapperContext);
        if (mapperContext.hasMode(LinkMode.MANAGED)) {
            if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE))
                throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + LinkMode.MANAGED + "] is not allowed for current user");
            if (mapperContext.hasModeButNot(TwinClassMode.LinkSrc2TwinClassMode.HIDE) && src.getSrcTwinClassId() != null)
                dst
                        .setSrcTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getSrcTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkSrc2TwinClassMode.SHORT)))
                        .setSrcTwinClassId(src.getSrcTwinClassId());
            if (mapperContext.hasModeButNot(UserMode.Link2UserMode.HIDE) && src.getCreatedByUserId() != null) {
                if (null == src.getCreatedByUser())
                    src.setCreatedByUser(userService.findEntitySafe(src.getCreatedByUserId()));
                dst
                        .setCreatedByUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Link2UserMode.SHORT))))
                        .setCreatedByUserId(src.getCreatedByUserId());
            }
        }
    }
}
