package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthLoginKeyRsV1")
public class AuthCryptKeyRsDTOv1 extends Response {
    @Schema(description = "public key to encrypt login")
    public CryptKeyDTOv1 publicKey;
}
