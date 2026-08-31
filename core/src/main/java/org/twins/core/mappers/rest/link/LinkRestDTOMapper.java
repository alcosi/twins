package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = LinkMode.class)
public class LinkRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {
    private final LinkService linkService;
    private final PermissionService permissionService;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = {TwinClassMode.LinkDst2TwinClassMode.class, TwinClassMode.LinkSrc2TwinClassMode.class,
            TwinClassMode.LinkRelationTwin2TwinClassMode.class})
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.Link2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkMode.DETAILED)) {
            case DETAILED, MANAGED:
                if (mapperContext.hasMode(LinkMode.MANAGED) && !permissionService.currentUserHasPermission(Permissions.LINK_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + LinkMode.MANAGED + "] is not allowed for current user");
                dst
                        .setSrcTwinClassId(src.getSrcTwinClassId())
                        .setBackwardName(I18nCacheHolder.addId(src.getBackwardNameI18NId()))
                        .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getForwardNameI18NId()))
                        .setDstTwinClassId(src.getDstTwinClassId())
                        .setRelationTwinClassId(src.getRelationTwinClassId())
                        .setLinkStrengthId(src.getLinkStrengthId())
                        .setType(src.getType());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getForwardNameI18NId()));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)
                || mapperContext.hasModeButNot(TwinClassMode.LinkSrc2TwinClassMode.HIDE)
                || (mapperContext.hasModeButNot(TwinClassMode.LinkRelationTwin2TwinClassMode.HIDE) && src.getRelationTwinClassId() != null)) {
            linkService.loadTwinClasses(src);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)) {
            dst.setDstTwinClassId(src.getDstTwinClassId());
            twinClassRestDTOMapper.postpone(src.getDstTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkDst2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkSrc2TwinClassMode.HIDE)) {
            dst.setSrcTwinClassId(src.getSrcTwinClassId());
            twinClassRestDTOMapper.postpone(src.getSrcTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkSrc2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkRelationTwin2TwinClassMode.HIDE) && src.getRelationTwinClassId() != null) {
            dst.setRelationTwinClassId(src.getRelationTwinClassId());
            twinClassRestDTOMapper.postpone(src.getRelationTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkRelationTwin2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserMode.Link2UserMode.HIDE) && src.getCreatedByUserId() != null) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            linkService.loadCreatedBy(src);
            userDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Link2UserMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(LinkMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<LinkEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.Link2UserMode.HIDE)) {
            linkService.loadCreatedBy(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)
                || mapperContext.hasModeButNot(TwinClassMode.LinkSrc2TwinClassMode.HIDE)
                || mapperContext.hasModeButNot(TwinClassMode.LinkRelationTwin2TwinClassMode.HIDE)) {
            linkService.loadTwinClasses(srcCollection);
        }
    }

}
