package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditCountDTOv1;
import org.twins.core.enums.domain.DomainVersionedTable;
import org.twins.core.enums.sort.DomainVersionAuditGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainVersionMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.domain.DomainVersionAuditService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DomainVersionAuditCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<DomainVersionAuditEntity, DomainVersionAuditGroupField>, DomainVersionAuditCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainVersionAudit2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = DomainVersionMode.DomainVersionAudit2DomainVersionMode.class)
    private final DomainVersionRestDTOMapper domainVersionRestDTOMapper;

    private final DomainVersionAuditService domainVersionAuditService;

    @Override
    public void map(CountResult<DomainVersionAuditEntity, DomainVersionAuditGroupField> src, DomainVersionAuditCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setDomainVersionId(entity.getDomainVersionId())
                // null unless grouped by tableName — fromTableName rejects nulls and unknowns
                .setTableName(entity.getTableName() == null ? null : DomainVersionedTable.fromTableName(entity.getTableName()))
                .setOperation(entity.getOperation())
                .setDomainOperationStatus(entity.getDomainOperationStatus())
                .setActorUserId(entity.getActorUserId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, UserMode.DomainVersionAudit2UserMode.HIDE, src, DomainVersionAuditGroupField.actorUserId)) {
            domainVersionAuditService.loadActorUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getActorUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainVersionAudit2UserMode.SHORT)));
        }
        if (needLoad(mapperContext, DomainVersionMode.DomainVersionAudit2DomainVersionMode.HIDE, src, DomainVersionAuditGroupField.domainVersionId)) {
            domainVersionAuditService.loadDomainVersion(entity);
            domainVersionRestDTOMapper.convertOrPostpone(entity.getDomainVersion(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DomainVersionMode.DomainVersionAudit2DomainVersionMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DomainVersionAuditEntity, DomainVersionAuditGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.DomainVersionAudit2UserMode.HIDE, someCount, DomainVersionAuditGroupField.actorUserId)) {
            domainVersionAuditService.loadActorUser(entityCollection);
        }
        if (needLoad(mapperContext, DomainVersionMode.DomainVersionAudit2DomainVersionMode.HIDE, someCount, DomainVersionAuditGroupField.domainVersionId)) {
            domainVersionAuditService.loadDomainVersion(entityCollection);
        }
    }
}
