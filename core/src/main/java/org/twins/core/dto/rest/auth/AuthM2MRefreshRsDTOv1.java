package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthM2MRefreshRsV1")
public class AuthM2MRefreshRsDTOv1 extends Response {
    @Schema(description = "tokens data")
    public Map<String, String> authData;

    @Schema(description = "public key to encrypt act as user data [optional]")
    public CryptKeyDTOv1 actAsUserPublicKey;
}
