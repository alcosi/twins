package org.twins.core.mappers.rest.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.dto.rest.auth.LoginKeyDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class LoginKeyRestDTOMapper extends RestSimpleDTOMapper<CryptKey.LoginPublicKey, LoginKeyDTOv1> {
    @Override
    public void map(CryptKey.LoginPublicKey src, LoginKeyDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setAlgorithm(src.getPublicKey().getAlgorithm())
                .setFormat(src.getPublicKey().getFormat())
                .setKey(Base64.getEncoder().encodeToString(src.getPublicKey().getEncoded()))
                .setExpiresAt(src.getExpires());
        if (src.getPublicKey() instanceof RSAPublicKey rsaPublicKey) {
            dst.setKeySize(rsaPublicKey.getModulus().bitLength());
        }
    }
}
