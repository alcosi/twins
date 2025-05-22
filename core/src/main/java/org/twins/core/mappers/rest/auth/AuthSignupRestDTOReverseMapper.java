package org.twins.core.mappers.rest.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.dto.rest.auth.AuthSignupRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class AuthSignupRestDTOReverseMapper extends RestSimpleDTOMapper<AuthSignupRqDTOv1, AuthSignup> {

    @Override
    public void map(AuthSignupRqDTOv1 src, AuthSignup dst, MapperContext mapperContext) throws Exception {
        dst
                .setEmail(src.getEmail())
                .setPassword(src.getPassword())
                .setFirstName(src.getFirstName())
                .setLastName(src.getLastName());
    }
}
