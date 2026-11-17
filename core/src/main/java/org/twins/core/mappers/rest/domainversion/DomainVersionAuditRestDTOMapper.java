package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditDTOv1;
import org.twins.core.enums.domain.DomainVersionedTable;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainVersionAuditMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainVersionMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.domain.DomainVersionAuditService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainVersionAuditMode.class)
public class DomainVersionAuditRestDTOMapper extends RestSimpleDTOMapper<DomainVersionAuditEntity, DomainVersionAuditDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainVersionAudit2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = DomainVersionMode.DomainVersionAudit2DomainVersionMode.class)
    private final DomainVersionRestDTOMapper domainVersionRestDTOMapper;

    private final DomainVersionAuditService domainVersionAuditService;

    @Override
    public void map(DomainVersionAuditEntity src, DomainVersionAuditDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainVersionAuditMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setDomainVersionId(src.getDomainVersionId())
                    .setSeq(src.getSeq())
                    .setTableName(DomainVersionedTable.fromTableName(src.getTableName()))
                    .setRowId(src.getRowId())
                    .setOperation(src.getOperation())
                    .setDomainOperationStatus(src.getDomainOperationStatus())
                    .setBeforeDomainVersionId(src.getBeforeDomainVersionId())
                    .setChangedAt(src.getChangedAt())
                    // row images can be heavy — DETAILED only
                    .setBefore(src.getBefore())
                    .setAfter(src.getAfter());
            case SHORT -> dst
                    .setId(src.getId())
                    .setDomainVersionId(src.getDomainVersionId())
                    .setSeq(src.getSeq())
                    .setTableName(DomainVersionedTable.fromTableName(src.getTableName()))
                    .setRowId(src.getRowId())
                    .setOperation(src.getOperation())
                    .setChangedAt(src.getChangedAt());
        }
        if (mapperContext.hasModeButNot(UserMode.DomainVersionAudit2UserMode.HIDE)) {
            dst.setActorUserId(src.getActorUserId());
            domainVersionAuditService.loadActorUser(src);
            userRestDTOMapper.postpone(src.getActorUser(), mapperContext.forkOnPoint(UserMode.DomainVersionAudit2UserMode.SHORT));
        }
        if (mapperContext.hasModeButNot(DomainVersionMode.DomainVersionAudit2DomainVersionMode.HIDE)) {
            domainVersionAuditService.loadDomainVersion(src);
            domainVersionRestDTOMapper.postpone(src.getDomainVersion(), mapperContext.forkOnPoint(DomainVersionMode.DomainVersionAudit2DomainVersionMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainVersionAuditEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (srcCollection.isEmpty()) return;
        // Batch-load actorUser and domainVersion (N+1 prevention)
        if (mapperContext.hasModeButNot(UserMode.DomainVersionAudit2UserMode.HIDE)) {
            domainVersionAuditService.loadActorUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(DomainVersionMode.DomainVersionAudit2DomainVersionMode.HIDE)) {
            domainVersionAuditService.loadDomainVersion(srcCollection);
        }
    }
}
