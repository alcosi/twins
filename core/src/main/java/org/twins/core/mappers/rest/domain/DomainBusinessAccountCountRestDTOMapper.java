package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domain.DomainBusinessAccountCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.notification.NotificationSchemaRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.domain.TierService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainBusinessAccountMode.class)
public class DomainBusinessAccountCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DomainBusinessAccountEntity>, DomainBusinessAccountCountDTOv1> {
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


    private final TierService tierService;
    private final DomainBusinessAccountService domainBusinessAccountService;

    @Override
    public void map(CountResult<DomainBusinessAccountEntity> src, DomainBusinessAccountCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setPermissionSchemaId(entity.getPermissionSchemaId())
                .setTwinflowSchemaId(entity.getTwinflowSchemaId())
                .setTwinClassSchemaId(entity.getTwinClassSchemaId())
                .setTierId(entity.getTierId())
                .setNotificationSchemaId(entity.getNotificationSchemaId())
                .setCount(src.getCount());
        if (mapperContext.hasModeButNot(TierMode.DomainBusinessAccount2TierMode.HIDE)) {
            domainBusinessAccountService.loadTier(entity);
            tierRestDTOMapper.postpone(entity.getTier(), mapperContext.forkOnPoint(TierMode.DomainBusinessAccount2TierMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.HIDE)) {
            domainBusinessAccountService.loadPermissionSchema(entity);
            permissionSchemaRestDTOMapper.postpone(entity.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinflowSchema(entity);
            twinflowSchemaRestDTOMapper.postpone(entity.getTwinflowSchema(), mapperContext.forkOnPoint(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinClassSchema(entity);
            twinclassSchemaDTOMapper.postpone(entity.getTwinClassSchema(), mapperContext.forkOnPoint(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE)) {
            domainBusinessAccountService.loadNotificationSchema(entity);
            notificationSchemaRestDTOMapper.postpone(entity.getNotificationSchema(), mapperContext.forkOnPoint(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.SHORT));
        }
    }

    public void beforeCollectionConversion(Collection<CountResult<DomainBusinessAccountEntity>> srcCollection, MapperContext mapperContext) throws Exception {
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinflowSchema(entityCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE)) {
            domainBusinessAccountService.loadTwinClassSchema(entityCollection);
        }
        if (mapperContext.hasModeButNot(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE)) {
            domainBusinessAccountService.loadNotificationSchema(entityCollection);
        }
        if (mapperContext.hasModeButNot(TierMode.DomainBusinessAccount2TierMode.HIDE)) {
            domainBusinessAccountService.loadTier(entityCollection);
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.HIDE)) {
            domainBusinessAccountService.loadPermissionSchema(entityCollection);
        }
    }
}
