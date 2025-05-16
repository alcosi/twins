package org.twins.core.mappers.rest.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.auth.TokensDTOv1;
import org.twins.core.featurer.identityprovider.ClientTokenData;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class ClientTokenRestDTOMapper extends RestSimpleDTOMapper<ClientTokenData, TokensDTOv1> {
    @Override
    public void map(ClientTokenData src, TokensDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setAuthToken(src.getAuthToken())
                .setRefreshToken(src.getRefreshToken());
    }
}
