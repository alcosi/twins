package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv2;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

@Component
@RequiredArgsConstructor
public class LinkForwardRestDTOV2Mapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv2> {
    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    private final I18nService i18nService;
    private final PermissionService permissionService;

    @MapperModePointerBinding(modes = TwinClassMode.LinkSrc2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.Link2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @Override
    public void map(LinkEntity src, LinkDTOv2 dst, MapperContext mapperContext) throws Exception {
        linkForwardRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasMode(LinkMode.MANAGED)) {
            if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE))
                throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + LinkMode.MANAGED + "] is not allowed for current user");
            dst
                    .setSrcTwinClassId(src.getSrcTwinClassId())
                    .setBackwardName(I18nCacheHolder.addId(src.getBackwardNameI18NId()))
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime());
            if (mapperContext.hasModeButNot(TwinClassMode.LinkSrc2TwinClassMode.HIDE) && src.getSrcTwinClassId() != null) {
                dst.setSrcTwinClassId(src.getSrcTwinClassId());
                twinClassRestDTOMapper.postpone(src.getSrcTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkSrc2TwinClassMode.SHORT));
            }
            if (mapperContext.hasModeButNot(UserMode.Link2UserMode.HIDE) && src.getCreatedByUserId() != null) {
                dst.setCreatedByUserId(src.getCreatedByUserId());
                //todo load
                userDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Link2UserMode.SHORT));
            }
        }
    }
}
