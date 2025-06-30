package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "CryptKeyV1")
public class CryptKeyDTOv1 {
    @Schema(description = "key id")
    public UUID id;

    @Schema(description = "")
    public String algorithm;

    @Schema(description = "")
    public String format;

    @Schema(description = "")
    public Integer keySize;

    @Schema(description = "")
    public String key;

    @Schema(description = "expires at")
    public LocalDateTime expiresAt;
}
