package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DomainCreateRestDTOReverseMapper extends RestSimpleDTOMapper<DomainCreateDTOv1, DomainEntity> {
    private final DomainSaveRestDTOReverseMapper domainSaveRestDTOReverseMapper;

    @Value(("${domain.resource.storage.default:00000000-0000-0000-0007-000000000001}"))
    private UUID defaultResourceStorageId;
    @Value(("${domain.attachment.storage.default:00000000-0000-0000-0007-000000000001}"))
    private UUID defaultAttachmentStorageId;

    @Override
    public void map(DomainCreateDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        domainSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setKey(src.getKey())
                .setDomainType(src.getType())
                .setResourcesStorageId(src.getResourceStorageId() == null ? defaultResourceStorageId : src.getResourceStorageId())
                .setAttachmentsStorageId(src.getAttachmentStorageId() == null ? defaultAttachmentStorageId : src.getAttachmentStorageId());
    }
}
