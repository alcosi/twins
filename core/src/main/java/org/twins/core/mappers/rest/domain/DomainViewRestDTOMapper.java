package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainViewDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceBasicRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;
import org.twins.core.mappers.rest.mappercontext.modes.FacePointerMode;
import org.twins.core.service.domain.DomainService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewDTOv1> {
    protected final DomainService domainService;
    protected final DomainViewPublicRestDTOMapper domainViewPublicRestDTOMapper;
    @MapperModePointerBinding(modes = FacePointerMode.DomainNavbar2FaceMode.class)
    protected final FaceBasicRestDTOMapper facePointerRestDTOMapper;

    @Override
    public void map(DomainEntity src, DomainViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        domainViewPublicRestDTOMapper.map(src, dst, mapperContext);

        switch (mapperContext.getModeOrUse(DomainMode.DETAILED)) {
            case DETAILED:
                dst
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
        if (mapperContext.hasModeButNot(FacePointerMode.DomainNavbar2FaceMode.HIDE)) {
            dst.setNavbar(facePointerRestDTOMapper.convert(src.getNavbarFace(), mapperContext.forkOnPoint(FacePointerMode.DomainNavbar2FaceMode.SHOW)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
