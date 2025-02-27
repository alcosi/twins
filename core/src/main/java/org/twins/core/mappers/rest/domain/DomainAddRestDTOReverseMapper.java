package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.twins.core.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DomainAddRestDTOReverseMapper extends RestSimpleDTOMapper<DomainCreateRqDTOv1, DomainEntity> {
    @Value(("${domain.resource.storage.default:00000000-0000-0000-0007-000000000001}"))
    private UUID defaultResourceStorageId;
    @Value(("${domain.attachment.storage.default:00000000-0000-0000-0007-000000000001}"))
    private UUID defaultAttachmentStorageId;
    private final I18nService i18nService;

    @Override
    public void map(DomainCreateRqDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setDescription(src.getDescription())
                .setDomainType(src.getType())
                .setResourcesStorageId(src.getResourceStorageId() == null ? defaultResourceStorageId : src.getResourceStorageId())
                .setAttachmentsStorageId(src.getAttachmentStorageId() == null ? defaultAttachmentStorageId : src.getAttachmentStorageId())
                .setDefaultI18nLocaleId(i18nService.localeFromTagOrSystemDefault(src.getDefaultLocale()));
    }
}
