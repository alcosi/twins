package org.twins.core.mappers.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewDTOv1> {

    @Override
    public void map(DomainEntity src, DomainViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainViewRestDTOMapper.DomainMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setDescription(src.getDescription())
                        .setType(src.getDomainType())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setDefaultLocale(src.getDefaultI18nLocaleId() != null ? src.getDefaultI18nLocaleId().getLanguage() : null)
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinClassSchemaId(src.getTwinClassSchemaId())
                        .setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }

    @AllArgsConstructor
    public enum DomainMode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
