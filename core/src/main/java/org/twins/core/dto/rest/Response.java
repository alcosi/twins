package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ErrorCodeCommon;

@Schema
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class Response {

    @Schema(description = "request processing status (see ErrorCode enum)", example = "0")
    private int status;
    @Schema(description = "User friendly, localized request processing status description", example = "success")
    private String msg;
    @Schema(description = "request processing status description, technical", example = "success")
    private String statusDetails;

    public Response() {
        this(ErrorCodeCommon.OK);
    }

    public Response(ErrorCode status) {
        this(status.getCode(), status.getMessage(), "");
    }
}

