package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domain.DomainBusinessAccountCountDTOv1;
import org.twins.core.enums.sort.DomainBusinessAccountGroupField;
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
public class DomainBusinessAccountCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DomainBusinessAccountEntity, DomainBusinessAccountGroupField>, DomainBusinessAccountCountDTOv1> {
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
    public void map(CountResult<DomainBusinessAccountEntity, DomainBusinessAccountGroupField> src, DomainBusinessAccountCountDTOv1 dst, MapperContext mapperContext) throws Exception {
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
        if (needLoad(mapperContext, TierMode.DomainBusinessAccount2TierMode.HIDE, src, DomainBusinessAccountGroupField.tierId)) {
            domainBusinessAccountService.loadTier(entity);
            tierRestDTOMapper.postpone(entity.getTier(), mapperContext.forkOnPoint(TierMode.DomainBusinessAccount2TierMode.SHORT));
        }
        if (needLoad(mapperContext, PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.HIDE, src, DomainBusinessAccountGroupField.permissionSchemaId)) {
            domainBusinessAccountService.loadPermissionSchema(entity);
            permissionSchemaRestDTOMapper.postpone(entity.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.SHORT));
        }
        if (needLoad(mapperContext, TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE, src, DomainBusinessAccountGroupField.twinflowSchemaId)) {
            domainBusinessAccountService.loadTwinflowSchema(entity);
            twinflowSchemaRestDTOMapper.postpone(entity.getTwinflowSchema(), mapperContext.forkOnPoint(TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.SHORT));
        }
        if (needLoad(mapperContext, TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE, src, DomainBusinessAccountGroupField.twinClassSchemaId)) {
            domainBusinessAccountService.loadTwinClassSchema(entity);
            twinclassSchemaDTOMapper.postpone(entity.getTwinClassSchema(), mapperContext.forkOnPoint(TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.SHORT));
        }
        if (needLoad(mapperContext, NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE, src, DomainBusinessAccountGroupField.notificationSchemaId)) {
            domainBusinessAccountService.loadNotificationSchema(entity);
            notificationSchemaRestDTOMapper.postpone(entity.getNotificationSchema(), mapperContext.forkOnPoint(NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.SHORT));
        }
    }

    public void beforeCollectionConversion(Collection<CountResult<DomainBusinessAccountEntity, DomainBusinessAccountGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinflowSchemaMode.DomainBusinessAccount2TwinflowSchemaMode.HIDE, someCount, DomainBusinessAccountGroupField.twinflowSchemaId)) {
            domainBusinessAccountService.loadTwinflowSchema(entityCollection);
        }
        if (needLoad(mapperContext, TwinClassSchemaMode.DomainBusinessAccount2TwinClassSchemaMode.HIDE, someCount, DomainBusinessAccountGroupField.twinClassSchemaId)) {
            domainBusinessAccountService.loadTwinClassSchema(entityCollection);
        }
        if (needLoad(mapperContext, NotificationSchemaMode.DomainBusinessAccount2NotificationSchemaMode.HIDE, someCount, DomainBusinessAccountGroupField.notificationSchemaId)) {
            domainBusinessAccountService.loadNotificationSchema(entityCollection);
        }
        if (needLoad(mapperContext, TierMode.DomainBusinessAccount2TierMode.HIDE, someCount, DomainBusinessAccountGroupField.tierId)) {
            domainBusinessAccountService.loadTier(entityCollection);
        }
        if (needLoad(mapperContext, PermissionSchemaMode.DomainBusinessAccount2PermissionSchemaMode.HIDE, someCount, DomainBusinessAccountGroupField.permissionSchemaId)) {
            domainBusinessAccountService.loadPermissionSchema(entityCollection);
        }
    }
}
