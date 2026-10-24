package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dto.rest.domainversion.DomainVersionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainVersionMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainVersionMode.class)
public class DomainVersionRestDTOMapper extends RestSimpleDTOMapper<DomainVersionEntity, DomainVersionDTOv1> {

    @Override
    public void map(DomainVersionEntity src, DomainVersionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainVersionMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setVersion(src.getVersion())
                    .setName(src.getName())
                    .setDescription(src.getDescription())
                    .setStatus(src.getStatus())
                    .setOrder(src.getOrder())
                    .setHash(src.getHash())
                    .setDeltaHash(src.getDeltaHash())
                    .setRollbackDeltaHash(src.getRollbackDeltaHash())
                    .setPreviousDomainVersionId(src.getPreviousDomainVersionId())
                    .setStartedAt(src.getStartedAt())
                    .setStartDuration(src.getStartDuration())
                    .setReleasedAt(src.getReleasedAt())
                    .setReleaseDuration(src.getReleaseDuration())
                    // snapshot/delta payloads can be heavy — DETAILED only
                    .setJsonFile(src.getJsonFile())
                    .setDeltaFile(src.getDeltaFile())
                    .setRollbackDeltaFile(src.getRollbackDeltaFile());
            case SHORT -> dst
                    .setId(src.getId())
                    .setVersion(src.getVersion())
                    .setName(src.getName())
                    .setStatus(src.getStatus())
                    .setOrder(src.getOrder())
                    .setStartedAt(src.getStartedAt())
                    .setReleasedAt(src.getReleasedAt());
        }
    }
}
