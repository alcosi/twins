package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Locale;


@Component
@RequiredArgsConstructor
public class DomainUserRestDTOMapper extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv1> {
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        // mapper context is null, used default DETAILED
        userRestDTOMapper.map(src.getUser(), dst, mapperContext);
        dst
                .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                .setCurrentLocale(src.getI18nLocaleId() != null ? src.getI18nLocaleId() : Locale.ROOT);
    }
}
