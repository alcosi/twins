package org.twins.core.dto.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ErrorCodeCommon;

@ApiModel
@EqualsAndHashCode
public class Response {
    @ApiModelProperty(notes = "request processing status (see ErrorCode enum)", example = "0")
    private int status;
    @ApiModelProperty(notes = "request processing status description", example = "success")
    private String msg;

    public Response() {
        this(ErrorCodeCommon.OK);
    }

    public Response(ErrorCode status) {
        this(status.getCode(), status.getMessage());
    }

    public Response(int status, String msg) {
        this.msg = msg;
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
