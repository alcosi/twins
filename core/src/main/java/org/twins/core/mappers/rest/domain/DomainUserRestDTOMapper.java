package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountUserDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserCollectionMode;
import org.twins.core.service.businessaccount.BusinessAccountService;

import java.util.Collection;
import java.util.Locale;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {BusinessAccountUserCollectionMode.class})
public class DomainUserRestDTOMapper extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv1> {

    private final BusinessAccountUserDTOMapper businessAccountUserDTOMapper;

    private final BusinessAccountService businessAccountService;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(BusinessAccountMode.DETAILED)) {
            case DETAILED:
                businessAccountService.loadBusinessAccounts(src);
                dst
                        .setId(src.getId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCurrentLocale(src.getI18nLocaleId() != null ? src.getI18nLocaleId() : Locale.ROOT);
                if (src.getBusinessAccountUserKit() != null) {
                    dst.setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
                    //todo need if?
                    if (mapperContext.hasMode(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.DETAILED))
                        convertOrPostpone(src.getBusinessAccountUserKit(), dst, businessAccountUserDTOMapper, mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.SHORT)), DomainUserDTOv1::setBusinessAccountUsers, DomainUserDTOv1::setBusinessAccountUserIdList);
                }
                break;
            case SHORT:
                dst.setId(src.getId());
                break;
        }
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
