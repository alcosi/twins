package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ErrorCodeCommon;

import java.util.Map;
import java.util.UUID;

@Schema
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class TwinChangeResponse extends Response {
    @Schema(description = "Invalid twin field id list")
    private Map<UUID, String> invalidTwinFieldErrors;

    public TwinChangeResponse() {
        super(ErrorCodeCommon.OK);
    }

    public TwinChangeResponse(ErrorCode status, Map<UUID, String> invalidTwinFieldErrors) {
        this(status.getCode(), status.getMessage(), "", invalidTwinFieldErrors);
    }

    public TwinChangeResponse(int status, String msg, String statusDetails, Map<UUID, String> invalidTwinFieldErrors) {
        super(status, msg, statusDetails);
        this.invalidTwinFieldErrors = invalidTwinFieldErrors;
    }
}
