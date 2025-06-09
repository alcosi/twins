package org.twins.core.mappers.rest.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthM2MGetToken;
import org.twins.core.dto.rest.auth.AuthM2MLoginRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class AuthM2MLoginRestDTOReverseMapper extends RestSimpleDTOMapper<AuthM2MLoginRqDTOv1, AuthM2MGetToken> {

    @Override
    public void map(AuthM2MLoginRqDTOv1 src, AuthM2MGetToken dst, MapperContext mapperContext) throws Exception {
        dst
                .setClientId(src.getClientId())
                .setClientSecret(src.getClientSecret())
                .setPublicKeyId(src.getPublicKeyId());
    }
}
