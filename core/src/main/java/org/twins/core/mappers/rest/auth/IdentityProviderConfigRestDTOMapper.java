package org.twins.core.mappers.rest.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.IdentityProviderConfig;
import org.twins.core.dto.rest.auth.AuthConfigDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class IdentityProviderConfigRestDTOMapper extends RestSimpleDTOMapper<IdentityProviderConfig, AuthConfigDTOv1> {
    private final AuthMethodRestDTOMapper authMethodRestDTOMapper;

    @Override
    public void map(IdentityProviderConfig src, AuthConfigDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getIdentityProvider().getName())
                .setAuthMethods(authMethodRestDTOMapper.convertCollection(src.getSupportedMethods()));
    }
}
