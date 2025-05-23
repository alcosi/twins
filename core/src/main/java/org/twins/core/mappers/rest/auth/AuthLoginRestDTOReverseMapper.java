package org.twins.core.mappers.rest.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthLogin;
import org.twins.core.dto.rest.auth.AuthLoginRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class AuthLoginRestDTOReverseMapper extends RestSimpleDTOMapper<AuthLoginRqDTOv1, AuthLogin> {

    @Override
    public void map(AuthLoginRqDTOv1 src, AuthLogin dst, MapperContext mapperContext) throws Exception {
        dst
                .setPassword(src.getPassword())
                .setUsername(src.getUsername())
                .setFingerPrint(src.getFingerprint())
                .setPublicKeyId(src.getPublicKeyId());
    }
}
