package org.twins.core.mappers.rest.auth;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.domain.auth.method.AuthMethodOath2;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.domain.auth.method.AuthMethodStub;
import org.twins.core.dto.rest.auth.methods.AuthMethodDTOv1;
import org.twins.core.dto.rest.auth.methods.AuthMethodOath2DTOv1;
import org.twins.core.dto.rest.auth.methods.AuthMethodPasswordDTOv1;
import org.twins.core.dto.rest.auth.methods.AuthMethodStubDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class AuthMethodRestDTOMapper extends RestSimpleDTOMapper<AuthMethod, AuthMethodDTOv1> {
    @Override
    public void map(AuthMethod src, AuthMethodDTOv1 dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public AuthMethodDTOv1 convert(AuthMethod src, MapperContext mapperContext) throws Exception {
        if (src instanceof AuthMethodStub authMethodStub) {
            return new AuthMethodStubDTOv1();
        } else if (src instanceof AuthMethodPassword authMethodPassword) {
            return new AuthMethodPasswordDTOv1()
                    .recoverSupported(authMethodPassword.isRecoverSupported())
                    .registerSupported(authMethodPassword.isRegisterSupported())
                    .fingerPrintRequired(authMethodPassword.isFingerprintRequired());
        } else if (src instanceof AuthMethodOath2 authMethodOath2) {
            return new AuthMethodOath2DTOv1()
                    .label(authMethodOath2.getLabel())
                    .iconUrl(authMethodOath2.getIconUrl())
                    .redirectUrl(authMethodOath2.getRedirectUrl());
        }
        return null;
    }
}
