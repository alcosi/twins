package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserCollectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.service.businessaccount.BusinessAccountService;

import java.util.Collection;
import java.util.Locale;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainUserMode.class, BusinessAccountUserCollectionMode.class})
public class DomainUserRestDTOMapper extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv1> {

    private final BusinessAccountService businessAccountService;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (showBusinessAccountUserCollection(mapperContext))
            businessAccountService.loadBusinessAccounts(src);
        switch (mapperContext.getModeOrUse(DomainUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCurrentLocale(src.getI18nLocaleId() != null ? src.getI18nLocaleId() : Locale.ROOT);
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId());
                break;
        }
        if (showBusinessAccountUserCollection(mapperContext))
            dst.setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
    }

    private static boolean showBusinessAccountUserCollection(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(BusinessAccountUserCollectionMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showBusinessAccountUserCollection(mapperContext))
            businessAccountService.loadBusinessAccounts(srcCollection);
    }
}
