package org.twins.core.mappers.rest.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class ClientSideAuthDateRestDTOMapper extends RestSimpleDTOMapper<ClientSideAuthData, HashMap<String, String>> {
    @Override
    public void map(ClientSideAuthData src, HashMap<String, String> dst, MapperContext mapperContext) throws Exception {
        dst
                .putAll(src);
    }
}
