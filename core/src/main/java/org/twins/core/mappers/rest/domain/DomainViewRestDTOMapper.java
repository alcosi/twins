package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewRsDTOv1> {

    @Override
    public void map(DomainEntity src, DomainViewRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setDescription(src.getDescription())
                .setType(src.getDomainType())
                .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                .setDefaultLocale(src.getDefaultI18nLocaleId() != null ? src.getDefaultI18nLocaleId().getLanguage() : null)
                .setTwinflowSchemaId(src.getTwinflowSchemaId())
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setTwinClassSchemaId(src.getTwinClassSchemaId())
                .setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId())
        ;
    }
}
