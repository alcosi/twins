package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dto.rest.domain.DomainBusinessAccountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.notification.NotificationSchemaRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainBusinessAccountMode.class})
public class DomainBusinessAccountDTOMapper extends RestSimpleDTOMapper<DomainBusinessAccountEntity, DomainBusinessAccountDTOv1> {
    @MapperModePointerBinding(modes = {BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.class})
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @MapperModePointerBinding(modes = {TierMode.DomainBusinessAccount2TierMode.class})
    private final TierRestDTOMapper tierRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.class)
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.class)
    private final TwinClassSchemaDTOMapper twinclassSchemaDTOMapper;

    @MapperModePointerBinding(modes = NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.class)
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;

    private final TwinService twinService;
    private final UserService userService;
    private final DomainBusinessAccountService domainBusinessAccountService;

    @Override
    public void map(DomainBusinessAccountEntity src, DomainBusinessAccountDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainBusinessAccountMode.DETAILED)) {
            case DETAILED:
                twinService.loadTwinCountForDomainBusinessAccount(src);
                userService.loadUserCountForDomainBusinessAccount(src);
                dst
                        .setId(src.getId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setTwinClassSchemaId(src.getTwinClassSchemaId())
                        .setTierId(src.getTierId())
                        .setTwinsCount(src.getTwinsCount())
                        .setActiveUsersCount(src.getUsersCount())
                        .setNotificationSchemaId(src.getNotificationSchemaId())
                        .setAttachmentsStorageUsedCount(src.getAttachmentsStorageUsedCount())
                        .setAttachmentsStorageUsedSize(src.getAttachmentsStorageUsedSize())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());

                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TierMode.DomainBusinessAccount2TierMode.HIDE)) {
            dst.setTierId(src.getTierId());
            tierRestDTOMapper.postpone(src.getTier(), mapperContext.forkOnPoint(TierMode.DomainBusinessAccount2TierMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinflowSchema(src);
            dst.setTwinflowSchemaId(src.getTwinflowSchemaId());
            twinflowSchemaRestDTOMapper.postpone(src.getTwinflowSchema(), mapperContext.forkOnPoint(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinClassSchema(src);
            dst.setTwinClassSchemaId(src.getTwinClassSchemaId());
            twinclassSchemaDTOMapper.postpone(src.getTwinClassSchema(), mapperContext.forkOnPoint(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE)) {
            domainBusinessAccountService.loadNotificationSchema(src);
            dst.setNotificationSchemaId(src.getNotificationSchemaId());
            notificationSchemaRestDTOMapper.postpone(src.getNotificationSchema(), mapperContext.forkOnPoint(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.SHORT));
        }
    }

    public void beforeCollectionConversion(Collection<DomainBusinessAccountEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasMode(DomainBusinessAccountMode.DETAILED)) {
            twinService.loadTwinCountForDomainBusinessAccounts(srcCollection);
            userService.loadUserCountForDomainBusinessAccounts(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinflowSchema(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinClassSchema(srcCollection);
        }
        if (mapperContext.hasModeButNot(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE)) {
            domainBusinessAccountService.loadNotificationSchema(srcCollection);
        }
    }
}
